package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.AttendanceRecord
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.Workshop
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface AttendanceRepository {
    fun getTeacherWorkshops(teacherId: String): Flow<List<Workshop>>
    suspend fun getStudentsForWorkshop(studentIds: List<String>): Result<List<User>>
    suspend fun saveAttendanceRecord(record: AttendanceRecord): Result<Unit>
}

class AttendanceRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AttendanceRepository {

    private val workshopsCollection get() = firestore.collection(WORKSHOPS_COLLECTION)
    private val usersCollection get() = firestore.collection(USERS_COLLECTION)
    private val attendancesCollection get() = firestore.collection(ATTENDANCES_COLLECTION)

    override fun getTeacherWorkshops(teacherId: String): Flow<List<Workshop>> = callbackFlow {
        val query = workshopsCollection.whereEqualTo("teacherId", teacherId)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val workshops = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Workshop::class.java)?.copy(id = doc.id)
            }.orEmpty()
            trySend(workshops)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getStudentsForWorkshop(studentIds: List<String>): Result<List<User>> =
        runCatching {
            if (studentIds.isEmpty()) return@runCatching emptyList()

            // Consulta particionada para respeitar o limite de 30 IDs do whereIn do Firestore
            studentIds.chunked(30).flatMap { chunk ->
                usersCollection
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(User::class.java)?.copy(id = doc.id)
                    }
            }
        }

    override suspend fun saveAttendanceRecord(record: AttendanceRecord): Result<Unit> =
        runCatching {
            val docRef = if (record.id.isNotBlank()) {
                attendancesCollection.document(record.id)
            } else {
                attendancesCollection.document()
            }
            val recordToSave = if (record.id.isBlank()) record.copy(id = docRef.id) else record
            docRef.set(recordToSave).await()
        }

    private companion object {
        const val WORKSHOPS_COLLECTION = "workshops"
        const val USERS_COLLECTION = "users"
        const val ATTENDANCES_COLLECTION = "attendances"
    }
}
