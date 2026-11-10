package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.Dependent
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface UserRepository {
    fun getPendingUsers(): Flow<List<User>>
    fun getApprovedUsers(): Flow<List<User>>
    suspend fun getAvailableStudents(): Result<List<User>>
    suspend fun linkDependentToGuardian(guardianId: String, student: User): Result<Unit>
    suspend fun unlinkDependent(guardianId: String, dependentId: String): Result<Unit>
    suspend fun updateUserStatus(userId: String, newStatus: UserStatus): Result<Unit>
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun addDependentToUser(userId: String, dependent: Dependent): Result<Unit>
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

    override fun getApprovedUsers(): Flow<List<User>> = callbackFlow {
        val query = usersCollection.whereEqualTo("status", UserStatus.APPROVED.name)
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

    override suspend fun getAvailableStudents(): Result<List<User>> = runCatching {
        val snapshot = usersCollection
            .whereEqualTo("role", UserRole.ALUNO.name)
            .get()
            .await()
        snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun linkDependentToGuardian(guardianId: String, student: User): Result<Unit> = runCatching {
        val dependent = Dependent(
            id = student.id,
            name = student.name,
            birthDate = student.birthDate,
            relationship = "Aluno(a) / Dependente",
            medicalNotes = student.medicalNotes,
            documents = student.documents,
        )
        val dependentData = mapOf(
            "id" to dependent.id,
            "name" to dependent.name,
            "birthDate" to dependent.birthDate,
            "relationship" to dependent.relationship,
            "medicalNotes" to dependent.medicalNotes,
            "enrolledWorkshopIds" to dependent.enrolledWorkshopIds,
        )
        usersCollection.document(guardianId)
            .update(
                mapOf(
                    "dependents" to FieldValue.arrayUnion(dependentData),
                    "associatedStudentIds" to FieldValue.arrayUnion(student.id),
                )
            )
            .await()
    }

    override suspend fun unlinkDependent(guardianId: String, dependentId: String): Result<Unit> = runCatching {
        val doc = usersCollection.document(guardianId).get().await()
        val user = doc.toObject(User::class.java)
        if (user != null) {
            val updatedDependents = user.dependents.filterNot { it.id == dependentId }
            val updatedAssociated = user.associatedStudentIds.filterNot { it == dependentId }
            usersCollection.document(guardianId)
                .update(
                    mapOf(
                        "dependents" to updatedDependents,
                        "associatedStudentIds" to updatedAssociated,
                    )
                )
                .await()
        }
    }

    override suspend fun addDependentToUser(userId: String, dependent: Dependent): Result<Unit> =
        runCatching {
            val dependentData = mapOf(
                "id" to dependent.id,
                "name" to dependent.name,
                "birthDate" to dependent.birthDate,
                "relationship" to dependent.relationship,
                "medicalNotes" to dependent.medicalNotes,
                "enrolledWorkshopIds" to dependent.enrolledWorkshopIds,
            )
            usersCollection.document(userId)
                .update(
                    mapOf(
                        "dependents" to FieldValue.arrayUnion(dependentData),
                        "associatedStudentIds" to FieldValue.arrayUnion(dependent.id),
                    )
                )
                .await()
        }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
