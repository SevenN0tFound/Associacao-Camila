package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.EligibleParticipant
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.data.model.Workshop
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface WorkshopRepository {
    fun getAllWorkshops(): Flow<List<Workshop>>
    suspend fun createWorkshop(workshop: Workshop): Result<Unit>
    suspend fun updateWorkshop(workshop: Workshop): Result<Unit>
    suspend fun deleteWorkshop(workshopId: String): Result<Unit>
    suspend fun enrollStudent(workshopId: String, studentId: String): Result<Unit>
    suspend fun unrollStudent(workshopId: String, studentId: String): Result<Unit>
    suspend fun toggleRegistrationStatus(workshopId: String, isOpen: Boolean): Result<Unit>
    suspend fun getApprovedTeachers(): Result<List<User>>
    suspend fun getApprovedStudents(): Result<List<User>>
    suspend fun getEligibleParticipants(): Result<List<EligibleParticipant>>
    suspend fun seedMockWorkshops(): Result<Unit>
}

class WorkshopRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : WorkshopRepository {

    private val workshopsCollection get() = firestore.collection(WORKSHOPS_COLLECTION)
    private val usersCollection get() = firestore.collection(USERS_COLLECTION)

    override fun getAllWorkshops(): Flow<List<Workshop>> = callbackFlow {
        val registration = workshopsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val w = doc.toObject(Workshop::class.java)?.copy(id = doc.id)
                    val rawList = (doc.get("enrolledStudentIds") as? List<*>)
                        ?: (doc.get("studentIds") as? List<*>)
                        ?: w?.enrolledStudentIds
                        ?: emptyList<Any>()
                    val dedupedIds = rawList.filterIsInstance<String>().distinct().filter { it.isNotBlank() }
                    w?.copy(enrolledStudentIds = dedupedIds)
                } catch (_: Exception) {
                    try {
                        val rawList = (doc.get("enrolledStudentIds") as? List<*>)
                            ?: (doc.get("studentIds") as? List<*>)
                            ?: emptyList<Any>()
                        Workshop(
                            id = doc.id,
                            name = doc.getString("name").orEmpty(),
                            description = doc.getString("description").orEmpty(),
                            teacherId = doc.getString("teacherId").orEmpty(),
                            enrolledStudentIds = rawList.filterIsInstance<String>().distinct().filter { it.isNotBlank() },
                            isRegistrationOpen = doc.getBoolean("isRegistrationOpen") ?: true,
                            registrationDeadline = doc.getLong("registrationDeadline"),
                            maxCapacity = doc.getLong("maxCapacity")?.toInt() ?: 0,
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }.orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun createWorkshop(workshop: Workshop): Result<Unit> = runCatching {
        val docRef = if (workshop.id.isNotBlank()) {
            workshopsCollection.document(workshop.id)
        } else {
            workshopsCollection.document()
        }
        val sanitized = workshop.copy(
            id = if (workshop.id.isBlank()) docRef.id else workshop.id,
            enrolledStudentIds = workshop.enrolledStudentIds.distinct().filter { it.isNotBlank() }
        )
        docRef.set(sanitized).await()
    }

    override suspend fun updateWorkshop(workshop: Workshop): Result<Unit> = runCatching {
        require(workshop.id.isNotBlank()) { "ID da oficina não pode ser vazio para atualização." }
        val sanitized = workshop.copy(
            enrolledStudentIds = workshop.enrolledStudentIds.distinct().filter { it.isNotBlank() }
        )
        workshopsCollection.document(sanitized.id).set(sanitized).await()
    }

    override suspend fun deleteWorkshop(workshopId: String): Result<Unit> = runCatching {
        require(workshopId.isNotBlank()) { "ID da oficina não pode ser vazio para exclusão." }
        workshopsCollection.document(workshopId).delete().await()
    }

    override suspend fun enrollStudent(workshopId: String, studentId: String): Result<Unit> = runCatching {
        require(workshopId.isNotBlank()) { "ID da oficina não pode ser vazio." }
        require(studentId.isNotBlank()) { "ID do aluno não pode ser vazio." }
        workshopsCollection.document(workshopId).update(
            "enrolledStudentIds",
            FieldValue.arrayUnion(studentId)
        ).await()
    }

    override suspend fun unrollStudent(workshopId: String, studentId: String): Result<Unit> = runCatching {
        require(workshopId.isNotBlank()) { "ID da oficina não pode ser vazio." }
        require(studentId.isNotBlank()) { "ID do aluno não pode ser vazio." }
        workshopsCollection.document(workshopId).update(
            "enrolledStudentIds",
            FieldValue.arrayRemove(studentId)
        ).await()
    }

    override suspend fun toggleRegistrationStatus(workshopId: String, isOpen: Boolean): Result<Unit> = runCatching {
        require(workshopId.isNotBlank()) { "ID da oficina não pode ser vazio." }
        workshopsCollection.document(workshopId).update(
            "isRegistrationOpen",
            isOpen
        ).await()
    }

    override suspend fun getApprovedTeachers(): Result<List<User>> = runCatching {
        val snapshot = usersCollection
            .whereEqualTo("role", UserRole.PROFESSOR.name)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject(User::class.java)?.copy(id = doc.id)
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun getApprovedStudents(): Result<List<User>> = runCatching {
        val snapshot = usersCollection
            .whereEqualTo("role", UserRole.ALUNO.name)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject(User::class.java)?.copy(id = doc.id)
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun getEligibleParticipants(): Result<List<EligibleParticipant>> = runCatching {
        // Busca usuários com perfil ALUNO aprovados
        val studentsSnapshot = usersCollection
            .whereEqualTo("role", UserRole.ALUNO.name)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .get()
            .await()

        val students = studentsSnapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject(User::class.java)?.copy(id = doc.id)
            } catch (_: Exception) {
                null
            }
        }

        // Busca usuários com perfil RESPONSAVEL aprovados para extrair seus dependentes e vínculos
        val responsiblesSnapshot = usersCollection
            .whereEqualTo("role", UserRole.RESPONSAVEL.name)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .get()
            .await()

        val responsibles = responsiblesSnapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject(User::class.java)?.copy(id = doc.id)
            } catch (_: Exception) {
                null
            }
        }

        // Mapeia vínculos de responsáveis por ID do aluno e por Nome
        val responsibleById = mutableMapOf<String, String>()
        val responsibleByName = mutableMapOf<String, Pair<String, String?>>()

        responsibles.forEach { resp ->
            val respName = resp.name.ifBlank { "Responsável" }
            resp.associatedStudentIds.forEach { studentId ->
                if (studentId.isNotBlank()) {
                    responsibleById[studentId] = respName
                }
            }
            resp.dependents.forEach { dep ->
                if (dep.id.isNotBlank()) {
                    responsibleById[dep.id] = respName
                }
                val depNormName = dep.name.trim().lowercase()
                if (depNormName.isNotBlank()) {
                    responsibleByName[depNormName] = respName to dep.relationship
                }
            }
        }

        // Constrói lista de ALUNO unificada (prioriza cadastro independente vinculando responsável se houver)
        val studentsList = students.map { user ->
            val normName = user.name.trim().lowercase()
            val respLink = responsibleById[user.id] ?: responsibleByName[normName]?.first
            val relationship = responsibleByName[normName]?.second

            EligibleParticipant(
                id = user.id,
                name = user.name.ifBlank { "Sem nome" },
                isDependent = respLink != null,
                responsibleName = respLink,
                relationship = relationship,
                email = user.email.ifBlank { null }
            )
        }

        val studentIds = students.map { it.id }.toSet()
        val studentNames = students.map { it.name.trim().lowercase() }.filter { it.isNotBlank() }.toSet()

        // Mapeia apenas dependentes que ainda NÃO possuem conta própria de ALUNO
        val dependentsList = responsibles.flatMap { resp ->
            val respName = resp.name.ifBlank { "Responsável" }
            resp.dependents
                .filter { dep ->
                    !studentIds.contains(dep.id) && !studentNames.contains(dep.name.trim().lowercase())
                }
                .map { dep ->
                    EligibleParticipant(
                        id = dep.id,
                        name = dep.name.ifBlank { "Sem nome" },
                        isDependent = true,
                        responsibleName = respName,
                        relationship = dep.relationship,
                        email = null
                    )
                }
        }

        (studentsList + dependentsList)
            .distinctBy { it.id }
            .distinctBy { it.name.trim().lowercase() }
            .sortedBy { it.name.lowercase() }
    }

    override suspend fun seedMockWorkshops(): Result<Unit> = runCatching {
        val teachers = getApprovedTeachers().getOrDefault(emptyList())
        val participants = getEligibleParticipants().getOrDefault(emptyList())

        val assignedTeacherId = teachers.firstOrNull()?.id.orEmpty()
        val allParticipantIds = participants.map { it.id }

        // Consulta oficinas já existentes para evitar duplicidade de nomes
        val existingSnapshot = workshopsCollection.get().await()
        val existingNames = existingSnapshot.documents.mapNotNull {
            it.getString("name")?.trim()?.lowercase()
        }.toSet()

        val defaultWorkshops = listOf(
            Workshop(
                name = "Oficina de Fortalecimento",
                description = "Encontros socioeducativos, desenvolvimento cidadão e apoio psicossocial às famílias.",
                teacherId = assignedTeacherId,
                enrolledStudentIds = allParticipantIds,
            ),
            Workshop(
                name = "Projeto Mídias Force",
                description = "Capacitação prática em comunicação comunitária, edição audiovisual e mídias sociais.",
                teacherId = assignedTeacherId,
                enrolledStudentIds = allParticipantIds.take(allParticipantIds.size.coerceAtLeast(1)),
            ),
            Workshop(
                name = "Projeto Interconexão",
                description = "Inclusão digital, tecnologia e robótica para o futuro profissional e acadêmico dos jovens.",
                teacherId = assignedTeacherId,
                enrolledStudentIds = allParticipantIds,
            ),
        )

        val batch = firestore.batch()
        var hasNewToInsert = false

        for (workshop in defaultWorkshops) {
            if (!existingNames.contains(workshop.name.trim().lowercase())) {
                val docRef = workshopsCollection.document()
                batch.set(docRef, workshop.copy(id = docRef.id))
                hasNewToInsert = true
            }
        }

        if (hasNewToInsert) {
            batch.commit().await()
        }
    }

    private companion object {
        const val WORKSHOPS_COLLECTION = "workshops"
        const val USERS_COLLECTION = "users"
    }
}
