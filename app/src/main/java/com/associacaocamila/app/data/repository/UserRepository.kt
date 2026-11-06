package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface UserRepository {
    fun getPendingUsers(): Flow<List<User>>
    suspend fun updateUserStatus(userId: String, newStatus: UserStatus): Result<Unit>
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit>
}

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : UserRepository {

    private val usersCollection get() = firestore.collection(USERS_COLLECTION)

    override fun getPendingUsers(): Flow<List<User>> = callbackFlow {
        val query = usersCollection.whereEqualTo("status", UserStatus.PENDING.name)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            }.orEmpty()
            trySend(users)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun updateUserStatus(userId: String, newStatus: UserStatus): Result<Unit> =
        runCatching {
            usersCollection.document(userId)
                .update("status", newStatus.name)
                .await()
        }

    override suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> =
        runCatching {
            usersCollection.document(userId)
                .update(updates)
                .await()
        }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
