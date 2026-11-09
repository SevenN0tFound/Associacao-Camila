package com.associacaocamila.app.data.model

import java.util.UUID

data class DocumentAttachment(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // "RG / CPF", "Comprovante de Residência", "Laudo Médico", "Outro"
    val fileUrl: String = "",
    val fileType: String = "", // "image/*", "application/pdf"
    val uploadedAt: Long = System.currentTimeMillis(),
)
