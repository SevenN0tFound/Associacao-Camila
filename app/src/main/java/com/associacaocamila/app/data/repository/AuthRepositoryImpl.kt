package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    private val usersCollection get() = firestore.collection(USERS_COLLECTION)

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User> = runCatching {
        val firebaseUser = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("Falha ao criar usuário: UID não disponível.")

        val user = User(
            id = firebaseUser.uid,
            name = name,
            email = email,
            role = role,
            status = UserStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )

        try {
            usersCollection.document(user.id).set(user).await()
        } catch (e: Exception) {
            // Evita conta "órfã" no Auth sem documento correspondente no Firestore.
            runCatching { firebaseUser.delete().await() }
            auth.signOut()
            throw e
        }
        user
    }

    override suspend fun signIn(email: String, password: String): Result<User> = runCatching {
        val firebaseUser = auth.signInWithEmailAndPassword(email, password).await().user
            ?: error("Falha ao autenticar: usuário não encontrado.")

        val snapshot = usersCollection.document(firebaseUser.uid).get().await()
        snapshot.toUser() ?: run {
            auth.signOut()
            error("Perfil do usuário não encontrado.")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCurrentUser(): Flow<User?> = authStateFlow().flatMapLatest { uid ->
        if (uid == null) flowOf(null) else userDocumentFlow(uid)
    }

    override fun signOut() = auth.signOut()

    /** Emite o UID atual (ou null) a cada mudança de estado de autenticação. */
    private fun authStateFlow(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Observa em tempo real o documento do usuário (ex.: aprovação pela ONG). */
    private fun userDocumentFlow(uid: String): Flow<User?> = callbackFlow {
        val registration = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else {
                trySend(snapshot?.toUser())
            }
        }
        awaitClose { registration.remove() }
    }

    private fun DocumentSnapshot.toUser(): User? =
        if (exists()) toObject(User::class.java)?.copy(id = id) else null

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
