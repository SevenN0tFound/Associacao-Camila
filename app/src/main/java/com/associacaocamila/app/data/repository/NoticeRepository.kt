package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.Notice
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface NoticeRepository {
    fun getNotices(): Flow<List<Notice>>
    suspend fun addNotice(notice: Notice): Result<Unit>
    suspend fun deleteNotice(noticeId: String): Result<Unit>
}

class NoticeRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : NoticeRepository {

    private val noticesCollection get() = firestore.collection(NOTICES_COLLECTION)

    override fun getNotices(): Flow<List<Notice>> = callbackFlow {
        val query = noticesCollection.orderBy("timestamp", Query.Direction.DESCENDING)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Notice::class.java)?.copy(id = doc.id)
            }.orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addNotice(notice: Notice): Result<Unit> = runCatching {
        val docRef = if (notice.id.isNotBlank()) {
            noticesCollection.document(notice.id)
        } else {
            noticesCollection.document()
        }
        val toSave = if (notice.id.isBlank()) notice.copy(id = docRef.id) else notice
        docRef.set(toSave).await()
    }

    override suspend fun deleteNotice(noticeId: String): Result<Unit> = runCatching {
        require(noticeId.isNotBlank()) { "ID do aviso não pode ser vazio para exclusão." }
        noticesCollection.document(noticeId).delete().await()
    }

    private companion object {
        const val NOTICES_COLLECTION = "notices"
    }
}
