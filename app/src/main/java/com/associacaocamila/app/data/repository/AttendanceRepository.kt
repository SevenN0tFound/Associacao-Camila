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
import java.text.SimpleDateFormat
import java.util.Locale

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
            val distinctIds = studentIds.distinct().filter { it.isNotBlank() }
            if (distinctIds.isEmpty()) return@runCatching emptyList()

            // Consulta particionada para respeitar o limite de 30 IDs do whereIn do Firestore
            distinctIds.chunked(30).flatMap { chunk ->
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
            }.distinctBy { it.id }
        }

    override suspend fun getParticipantsForWorkshop(participantIds: List<String>): Result<List<EligibleParticipant>> =
        runCatching {
            val distinctIds = participantIds.distinct().filter { it.isNotBlank() }
            if (distinctIds.isEmpty()) return@runCatching emptyList()

            val participantIdSet = distinctIds.toSet()

            // 1. Busca usuários correspondentes diretamente por ID (Alunos)
            val usersList = distinctIds.chunked(30).flatMap { chunk ->
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
                    } catch (_: Exception) {
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

            // Preserva a ordem informada em distinctIds e desduplica
            val allMap = (studentParticipants + dependentParticipants).associateBy { it.id }
            distinctIds.mapNotNull { allMap[it] }.distinctBy { it.id }
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

            // 1. Consulta documentos na coleção padrão "attendances" por workshopId
            val primaryDocs = attendancesCollection
                .whereEqualTo("workshopId", workshopId)
                .get()
                .await()
                .documents

            // 2. Fallback de coleção: consulta também na coleção alternativa "attendance" se houver
            val alternativeDocs = try {
                firestore.collection("attendance")
                    .whereEqualTo("workshopId", workshopId)
                    .get()
                    .await()
                    .documents
            } catch (_: Exception) {
                emptyList()
            }

            // 3. Fallback de campo: caso registros antigos utilizem "turmaId"
            val turmaFallbackDocs = if (primaryDocs.isEmpty() && alternativeDocs.isEmpty()) {
                try {
                    attendancesCollection
                        .whereEqualTo("turmaId", workshopId)
                        .get()
                        .await()
                        .documents
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

            val allDocs = (primaryDocs + alternativeDocs + turmaFallbackDocs).distinctBy { it.id }

            // Mapeia todos os documentos garantindo que presenças E faltas sejam extraídas integralmente
            allDocs.mapNotNull { doc ->
                try {
                    val parsed = doc.toObject(AttendanceRecord::class.java)?.copy(id = doc.id)
                    if (parsed != null && parsed.attendanceMap.isNotEmpty()) {
                        val fullStatus = parsed.statusMap.toMutableMap()
                        parsed.attendanceMap.forEach { (sId, isPres) ->
                            if (!fullStatus.containsKey(sId)) {
                                fullStatus[sId] = if (isPres) "PRESENTE" else "FALTA"
                            }
                        }
                        return@mapNotNull parsed.copy(statusMap = fullStatus)
                    }
                } catch (_: Exception) { }

                // Fallback de deserialização manual detalhado (puxando TODOS os registros de presença e falta)
                try {
                    val dateMillis: Long = doc.getLong("dateMillis")
                        ?: doc.getTimestamp("date")?.toDate()?.time
                        ?: doc.getTimestamp("timestamp")?.toDate()?.time
                        ?: doc.getLong("date")
                        ?: runCatching {
                            doc.getString("date")?.let { str ->
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(str)?.time
                                    ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(str)?.time
                            }
                        }.getOrNull()
                        ?: System.currentTimeMillis()

                    val attendanceMap = mutableMapOf<String, Boolean>()

                    // Extração de mapa (attendanceMap ou records ou frequencia)
                    val rawMap = (doc.get("attendanceMap") as? Map<*, *>)
                        ?: (doc.get("records") as? Map<*, *>)
                        ?: (doc.get("frequencia") as? Map<*, *>)

                    rawMap?.forEach { (k, v) ->
                        val key = k?.toString() ?: return@forEach
                        when (v) {
                            is Boolean -> attendanceMap[key] = v
                            is String -> attendanceMap[key] = (v.equals("PRESENTE", ignoreCase = true) || v.equals("true", ignoreCase = true))
                            is Number -> attendanceMap[key] = (v.toInt() == 1)
                            is Map<*, *> -> {
                                val presentVal = v["present"] ?: v["isPresent"] ?: v["status"]
                                val isPres = when (presentVal) {
                                    is Boolean -> presentVal
                                    is String -> presentVal.equals("PRESENTE", ignoreCase = true) || presentVal.equals("true", ignoreCase = true)
                                    else -> true
                                }
                                attendanceMap[key] = isPres
                            }
                        }
                    }

                    // Extração de lista de alunos (records ou students)
                    val rawList = (doc.get("records") as? List<*>) ?: (doc.get("students") as? List<*>)
                    rawList?.filterIsInstance<Map<*, *>>()?.forEach { item ->
                        val sId = (item["studentId"] ?: item["id"] ?: item["userId"])?.toString() ?: return@forEach
                        val presentVal = item["present"] ?: item["isPresent"] ?: item["status"]
                        val isPres = when (presentVal) {
                            is Boolean -> presentVal
                            is String -> presentVal.equals("PRESENTE", ignoreCase = true) || presentVal.equals("true", ignoreCase = true)
                            else -> true
                        }
                        attendanceMap[sId] = isPres
                    }

                    // Se for documento individual por aluno
                    val singleStudentId = doc.getString("studentId") ?: doc.getString("userId")
                    if (singleStudentId != null && singleStudentId.isNotBlank()) {
                        val isPres = doc.getBoolean("present")
                            ?: doc.getBoolean("isPresent")
                            ?: (doc.getString("status")?.equals("PRESENTE", ignoreCase = true) == true)
                        attendanceMap[singleStudentId] = isPres
                    }

                    val statusMap = mutableMapOf<String, String>()
                    val rawStatus = (doc.get("statusMap") as? Map<*, *>)
                    rawStatus?.forEach { (k, v) ->
                        if (k != null && v != null) {
                            statusMap[k.toString()] = v.toString()
                        }
                    }
                    attendanceMap.forEach { (sId, isPres) ->
                        if (!statusMap.containsKey(sId)) {
                            statusMap[sId] = if (isPres) "PRESENTE" else "FALTA"
                        }
                    }

                    AttendanceRecord(
                        id = doc.id,
                        workshopId = doc.getString("workshopId") ?: doc.getString("turmaId").orEmpty(),
                        dateMillis = dateMillis,
                        teacherId = doc.getString("teacherId").orEmpty(),
                        attendanceMap = attendanceMap,
                        statusMap = statusMap,
                    )
                } catch (_: Exception) {
                    null
                }
            }
        }

    private companion object {
        const val WORKSHOPS_COLLECTION = "workshops"
        const val USERS_COLLECTION = "users"
        const val ATTENDANCES_COLLECTION = "attendances"
    }
}
