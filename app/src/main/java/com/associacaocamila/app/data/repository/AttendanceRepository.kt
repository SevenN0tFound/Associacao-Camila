package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.AttendanceRecord
import com.associacaocamila.app.data.model.EligibleParticipant
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
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
    suspend fun getParticipantsForWorkshop(participantIds: List<String>): Result<List<EligibleParticipant>>
    suspend fun saveAttendanceRecord(record: AttendanceRecord): Result<Unit>
    suspend fun getAttendanceRecordsForWorkshop(workshopId: String): Result<List<AttendanceRecord>>
}

class AttendanceRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AttendanceRepository {

    private val workshopsCollection get() = firestore.collection(WORKSHOPS_COLLECTION)
    private val usersCollection get() = firestore.collection(USERS_COLLECTION)
    private val attendancesCollection get() = firestore.collection(ATTENDANCES_COLLECTION)

    override fun getTeacherWorkshops(teacherId: String): Flow<List<Workshop>> = callbackFlow {
        if (teacherId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val query = workshopsCollection.whereEqualTo("teacherId", teacherId)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val workshops = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Workshop::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    try {
                        Workshop(
                            id = doc.id,
                            name = doc.getString("name").orEmpty(),
                            description = doc.getString("description").orEmpty(),
                            teacherId = doc.getString("teacherId").orEmpty(),
                            enrolledStudentIds = (doc.get("enrolledStudentIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            isRegistrationOpen = doc.getBoolean("isRegistrationOpen") ?: true,
                            registrationDeadline = doc.getLong("registrationDeadline"),
                            maxCapacity = doc.getLong("maxCapacity")?.toInt() ?: 0,
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
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
                        try {
                            doc.toObject(User::class.java)?.copy(id = doc.id)
                        } catch (_: Exception) {
                            null
                        }
                    }
            }
        }

    override suspend fun getParticipantsForWorkshop(participantIds: List<String>): Result<List<EligibleParticipant>> =
        runCatching {
            if (participantIds.isEmpty()) return@runCatching emptyList()

            val participantIdSet = participantIds.toSet()

            // 1. Busca usuários correspondentes diretamente por ID (Alunos)
            val usersList = participantIds.chunked(30).flatMap { chunk ->
                usersCollection
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        try {
                            doc.toObject(User::class.java)?.copy(id = doc.id)
                        } catch (_: Exception) {
                            null
                        }
                    }
            }

            val studentParticipants = usersList.map { user ->
                EligibleParticipant(
                    id = user.id,
                    name = user.name.ifBlank { "Sem nome" },
                    isDependent = false,
                    email = user.email
                )
            }

            val foundIds = studentParticipants.map { it.id }.toSet()
            val remainingIds = participantIdSet - foundIds

            // 2. Se houver IDs não encontrados diretamente na coleção de usuários,
            // busca entre os dependentes de usuários com role RESPONSAVEL
            val dependentParticipants = if (remainingIds.isNotEmpty()) {
                val responsiblesSnapshot = usersCollection
                    .whereEqualTo("role", UserRole.RESPONSAVEL.name)
                    .whereEqualTo("status", UserStatus.APPROVED.name)
                    .get()
                    .await()

                responsiblesSnapshot.documents.flatMap { doc ->
                    val responsible = try {
                        doc.toObject(User::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    } ?: return@flatMap emptyList()
                    responsible.dependents
                        .filter { it.id in remainingIds }
                        .map { dependent ->
                            EligibleParticipant(
                                id = dependent.id,
                                name = dependent.name.ifBlank { "Sem nome" },
                                isDependent = true,
                                responsibleName = responsible.name.ifBlank { "Responsável" },
                                relationship = dependent.relationship,
                                email = null
                            )
                        }
                }
            } else {
                emptyList()
            }

            // Preserva a ordem informada em participantIds
            val allMap = (studentParticipants + dependentParticipants).associateBy { it.id }
            participantIds.mapNotNull { allMap[it] }
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

    override suspend fun getAttendanceRecordsForWorkshop(workshopId: String): Result<List<AttendanceRecord>> =
        runCatching {
            if (workshopId.isBlank()) return@runCatching emptyList()
            attendancesCollection
                .whereEqualTo("workshopId", workshopId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(AttendanceRecord::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        try {
                            @Suppress("UNCHECKED_CAST")
                            AttendanceRecord(
                                id = doc.id,
                                workshopId = doc.getString("workshopId").orEmpty(),
                                dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                                teacherId = doc.getString("teacherId").orEmpty(),
                                attendanceMap = (doc.get("attendanceMap") as? Map<String, Boolean>) ?: emptyMap(),
                                statusMap = (doc.get("statusMap") as? Map<String, String>) ?: emptyMap(),
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
        }

    private companion object {
        const val WORKSHOPS_COLLECTION = "workshops"
        const val USERS_COLLECTION = "users"
        const val ATTENDANCES_COLLECTION = "attendances"
    }
}
