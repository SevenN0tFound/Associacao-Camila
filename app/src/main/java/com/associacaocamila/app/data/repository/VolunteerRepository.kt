package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.Volunteer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface VolunteerRepository {
    suspend fun submitVolunteer(volunteer: Volunteer): Result<Unit>
    fun getVolunteers(): Flow<List<Volunteer>>
    suspend fun updateVolunteerStatus(id: String, status: String): Result<Unit>
}

class VolunteerRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : VolunteerRepository {

    private val volunteersCollection get() = firestore.collection(VOLUNTEERS_COLLECTION)

    override suspend fun submitVolunteer(volunteer: Volunteer): Result<Unit> = runCatching {
        val docRef = if (volunteer.id.isNotBlank()) {
            volunteersCollection.document(volunteer.id)
        } else {
            volunteersCollection.document()
        }
        val toSave = if (volunteer.id.isBlank()) volunteer.copy(id = docRef.id) else volunteer
        docRef.set(toSave).await()
    }

    override fun getVolunteers(): Flow<List<Volunteer>> = callbackFlow {
        val query = volunteersCollection.orderBy("submittedAt", Query.Direction.DESCENDING)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Volunteer::class.java)?.copy(id = doc.id)
            }.orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun updateVolunteerStatus(id: String, status: String): Result<Unit> = runCatching {
        require(id.isNotBlank()) { "ID do voluntário não pode ser vazio." }
        volunteersCollection.document(id).update("status", status).await()
    }

    private companion object {
        const val VOLUNTEERS_COLLECTION = "volunteers"
    }
}
