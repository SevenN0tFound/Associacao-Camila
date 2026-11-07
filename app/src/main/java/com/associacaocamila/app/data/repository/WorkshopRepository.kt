package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.EligibleParticipant
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.data.model.Workshop
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
                doc.toObject(Workshop::class.java)?.copy(id = doc.id)
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
        val itemToSave = if (workshop.id.isBlank()) workshop.copy(id = docRef.id) else workshop
        docRef.set(itemToSave).await()
    }

    override suspend fun updateWorkshop(workshop: Workshop): Result<Unit> = runCatching {
        require(workshop.id.isNotBlank()) { "ID da oficina não pode ser vazio para atualização." }
        workshopsCollection.document(workshop.id).set(workshop).await()
    }

    override suspend fun deleteWorkshop(workshopId: String): Result<Unit> = runCatching {
        require(workshopId.isNotBlank()) { "ID da oficina não pode ser vazio para exclusão." }
        workshopsCollection.document(workshopId).delete().await()
    }

    override suspend fun getApprovedTeachers(): Result<List<User>> = runCatching {
        val snapshot = usersCollection
            .whereEqualTo("role", UserRole.PROFESSOR.name)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getApprovedStudents(): Result<List<User>> = runCatching {
        val snapshot = usersCollection
            .whereEqualTo("role", UserRole.ALUNO.name)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getEligibleParticipants(): Result<List<EligibleParticipant>> = runCatching {
        // Busca usuários com perfil ALUNO aprovados
        val studentsSnapshot = usersCollection
            .whereEqualTo("role", UserRole.ALUNO.name)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .get()
            .await()

        val studentsList = studentsSnapshot.documents.mapNotNull { doc ->
            val user = doc.toObject(User::class.java)?.copy(id = doc.id) ?: return@mapNotNull null
            EligibleParticipant(
                id = user.id,
                name = user.name.ifBlank { "Sem nome" },
                isDependent = false,
                email = user.email
            )
        }

        // Busca usuários com perfil RESPONSAVEL aprovados para extrair seus dependentes
        val responsiblesSnapshot = usersCollection
            .whereEqualTo("role", UserRole.RESPONSAVEL.name)
            .whereEqualTo("status", UserStatus.APPROVED.name)
            .get()
            .await()

        val dependentsList = responsiblesSnapshot.documents.flatMap { doc ->
            val responsible = doc.toObject(User::class.java)?.copy(id = doc.id) ?: return@flatMap emptyList()
            responsible.dependents.map { dependent ->
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

        studentsList + dependentsList
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
