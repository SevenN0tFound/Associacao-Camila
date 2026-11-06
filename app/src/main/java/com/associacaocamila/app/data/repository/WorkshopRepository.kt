package com.associacaocamila.app.data.repository

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
    suspend fun getApprovedTeachers(): Result<List<User>>
    suspend fun getApprovedStudents(): Result<List<User>>
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

    override suspend fun seedMockWorkshops(): Result<Unit> = runCatching {
        val teachers = getApprovedTeachers().getOrDefault(emptyList())
        val students = getApprovedStudents().getOrDefault(emptyList())

        val assignedTeacherId = teachers.firstOrNull()?.id.orEmpty()
        val allStudentIds = students.map { it.id }

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
                enrolledStudentIds = allStudentIds,
            ),
            Workshop(
                name = "Projeto Mídias Force",
                description = "Capacitação prática em comunicação comunitária, edição audiovisual e mídias sociais.",
                teacherId = assignedTeacherId,
                enrolledStudentIds = allStudentIds.take(allStudentIds.size.coerceAtLeast(1)),
            ),
            Workshop(
                name = "Projeto Interconexão",
                description = "Inclusão digital, tecnologia e robótica para o futuro profissional e acadêmico dos jovens.",
                teacherId = assignedTeacherId,
                enrolledStudentIds = allStudentIds,
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
