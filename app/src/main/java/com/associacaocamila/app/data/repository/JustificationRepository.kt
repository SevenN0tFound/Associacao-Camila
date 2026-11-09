package com.associacaocamila.app.data.repository

import android.net.Uri
import com.associacaocamila.app.data.model.AbsenceJustification
import com.associacaocamila.app.data.model.AttendanceStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface JustificationRepository {
    suspend fun submitJustification(
        justification: AbsenceJustification,
        fileUri: Uri? = null,
        mimeType: String? = null,
    ): Result<Unit>

    fun getJustificationsForStudent(studentId: String): Flow<List<AbsenceJustification>>
    fun getPendingJustifications(): Flow<List<AbsenceJustification>>
    suspend fun approveJustification(justification: AbsenceJustification): Result<Unit>
    suspend fun rejectJustification(justificationId: String): Result<Unit>
}

class JustificationRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) : JustificationRepository {

    private val justificationsCollection get() = firestore.collection(JUSTIFICATIONS_COLLECTION)
    private val attendancesCollection get() = firestore.collection(ATTENDANCES_COLLECTION)

    override suspend fun submitJustification(
        justification: AbsenceJustification,
        fileUri: Uri?,
        mimeType: String?,
    ): Result<Unit> = runCatching {
        var finalAttachmentUrl = justification.attachmentUrl

        if (fileUri != null) {
            val extension = when {
                mimeType?.contains("pdf", ignoreCase = true) == true -> "pdf"
                mimeType?.contains("png", ignoreCase = true) == true -> "png"
                else -> "jpg"
            }
            val storagePath = "justifications/${justification.id}_${System.currentTimeMillis()}.$extension"
            val storageRef = storage.reference.child(storagePath)
            storageRef.putFile(fileUri).await()
            finalAttachmentUrl = storageRef.downloadUrl.await().toString()
        }

        val docToSave = justification.copy(
            attachmentUrl = finalAttachmentUrl,
            status = "PENDENTE",
            requestedAt = System.currentTimeMillis()
        )

        justificationsCollection.document(docToSave.id).set(docToSave).await()
    }

    override fun getJustificationsForStudent(studentId: String): Flow<List<AbsenceJustification>> = callbackFlow {
        if (studentId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val query = justificationsCollection.whereEqualTo("studentId", studentId)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(AbsenceJustification::class.java)?.copy(id = doc.id)
            }.orEmpty().sortedByDescending { it.requestedAt }

            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    override fun getPendingJustifications(): Flow<List<AbsenceJustification>> = callbackFlow {
        val query = justificationsCollection.whereEqualTo("status", "PENDENTE")
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(AbsenceJustification::class.java)?.copy(id = doc.id)
            }.orEmpty().sortedByDescending { it.requestedAt }

            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun approveJustification(justification: AbsenceJustification): Result<Unit> = runCatching {
        justificationsCollection.document(justification.id)
            .update("status", "APROVADO")
            .await()

        if (justification.attendanceRecordId.isNotBlank()) {
            attendancesCollection.document(justification.attendanceRecordId)
                .update(
                    mapOf(
                        "statusMap.${justification.studentId}" to AttendanceStatus.JUSTIFICADO.name,
                        "attendanceMap.${justification.studentId}" to true // Presença atestada/justificada
                    )
                )
                .await()
        }
    }

    override suspend fun rejectJustification(justificationId: String): Result<Unit> = runCatching {
        justificationsCollection.document(justificationId)
            .update("status", "RECUSADO")
            .await()
    }

    private companion object {
        const val JUSTIFICATIONS_COLLECTION = "justifications"
        const val ATTENDANCES_COLLECTION = "attendances"
    }
}
