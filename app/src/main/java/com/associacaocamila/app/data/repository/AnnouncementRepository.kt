package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface AnnouncementRepository {
    fun getAnnouncements(): Flow<List<Announcement>>
    suspend fun postAnnouncement(announcement: Announcement): Result<Unit>
    suspend fun deleteAnnouncement(id: String): Result<Unit>
}

class AnnouncementRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AnnouncementRepository {

    private val announcementsCollection get() = firestore.collection(ANNOUNCEMENTS_COLLECTION)

    override fun getAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val query = announcementsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Announcement::class.java)?.copy(id = doc.id)
            }.orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun postAnnouncement(announcement: Announcement): Result<Unit> = runCatching {
        val docRef = if (announcement.id.isNotBlank()) {
            announcementsCollection.document(announcement.id)
        } else {
            announcementsCollection.document()
        }
        val toSave = if (announcement.id.isBlank()) announcement.copy(id = docRef.id) else announcement
        docRef.set(toSave).await()
    }

    override suspend fun deleteAnnouncement(id: String): Result<Unit> = runCatching {
        require(id.isNotBlank()) { "ID do comunicado não pode ser vazio para exclusão." }
        announcementsCollection.document(id).delete().await()
    }

    private companion object {
        const val ANNOUNCEMENTS_COLLECTION = "announcements"
    }
}
