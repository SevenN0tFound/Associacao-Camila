package com.associacaocamila.app.data.repository

import android.net.Uri
import com.associacaocamila.app.data.model.DocumentAttachment
import com.associacaocamila.app.data.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface DocumentRepository {
    suspend fun uploadDocument(
        ownerId: String,
        categoryName: String,
        fileUri: Uri,
        mimeType: String,
    ): Result<DocumentAttachment>

    suspend fun deleteDocument(
        ownerId: String,
        documentId: String,
        fileUrl: String,
    ): Result<Unit>
}

class DocumentRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) : DocumentRepository {

    private val usersCollection get() = firestore.collection("users")

    override suspend fun uploadDocument(
        ownerId: String,
        categoryName: String,
        fileUri: Uri,
        mimeType: String,
    ): Result<DocumentAttachment> = runCatching {
        val sanitizedCategory = categoryName.replace("/", "-").replace(" ", "_").trim()
        val timestamp = System.currentTimeMillis()
        val storagePath = "documents/$ownerId/${timestamp}_$sanitizedCategory"

        val storageRef = storage.reference.child(storagePath)
        storageRef.putFile(fileUri).await()

        val downloadUrl = storageRef.downloadUrl.await().toString()

        val attachment = DocumentAttachment(
            id = UUID.randomUUID().toString(),
            name = categoryName,
            fileUrl = downloadUrl,
            fileType = mimeType,
            uploadedAt = timestamp,
        )

        val attachmentMap = mapOf(
            "id" to attachment.id,
            "name" to attachment.name,
            "fileUrl" to attachment.fileUrl,
            "fileType" to attachment.fileType,
            "uploadedAt" to attachment.uploadedAt,
        )

        // Verifica se ownerId é um usuário direto
        val userDoc = usersCollection.document(ownerId).get().await()
        if (userDoc.exists()) {
            usersCollection.document(ownerId)
                .update("documents", FieldValue.arrayUnion(attachmentMap))
                .await()
        } else {
            // Se não for um usuário direto, procura o usuário responsável que contém esse dependente
            val parentUserSnapshot = usersCollection
                .whereArrayContains("associatedStudentIds", ownerId)
                .limit(1)
                .get()
                .await()

            if (!parentUserSnapshot.isEmpty) {
                val parentDoc = parentUserSnapshot.documents.first()
                val parentUser = parentDoc.toObject(User::class.java)
                if (parentUser != null) {
                    val updatedDependents = parentUser.dependents.map { dep ->
                        if (dep.id == ownerId) {
                            dep.copy(documents = dep.documents + attachment)
                        } else {
                            dep
                        }
                    }
                    parentDoc.reference.update("dependents", updatedDependents).await()
                }
            } else {
                // Fallback: tenta atualizar o documento diretamente se ele existir em users
                usersCollection.document(ownerId)
                    .update("documents", FieldValue.arrayUnion(attachmentMap))
                    .await()
            }
        }

        attachment
    }

    override suspend fun deleteDocument(
        ownerId: String,
        documentId: String,
        fileUrl: String,
    ): Result<Unit> = runCatching {
        // Tenta remover do Firebase Storage se houver URL válida
        if (fileUrl.isNotBlank()) {
            runCatching {
                storage.getReferenceFromUrl(fileUrl).delete().await()
            }
        }

        val userDoc = usersCollection.document(ownerId).get().await()
        if (userDoc.exists()) {
            val user = userDoc.toObject(User::class.java)
            if (user != null) {
                val updatedDocs = user.documents.filterNot { it.id == documentId }
                usersCollection.document(ownerId).update("documents", updatedDocs).await()
            }
        } else {
            val parentUserSnapshot = usersCollection
                .whereArrayContains("associatedStudentIds", ownerId)
                .limit(1)
                .get()
                .await()

            if (!parentUserSnapshot.isEmpty) {
                val parentDoc = parentUserSnapshot.documents.first()
                val parentUser = parentDoc.toObject(User::class.java)
                if (parentUser != null) {
                    val updatedDependents = parentUser.dependents.map { dep ->
                        if (dep.id == ownerId) {
                            dep.copy(documents = dep.documents.filterNot { it.id == documentId })
                        } else {
                            dep
                        }
                    }
                    parentDoc.reference.update("dependents", updatedDependents).await()
                }
            }
        }
    }
}
