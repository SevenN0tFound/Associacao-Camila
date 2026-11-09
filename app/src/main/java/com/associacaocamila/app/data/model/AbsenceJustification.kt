package com.associacaocamila.app.data.model

import java.util.UUID

data class AbsenceJustification(
    val id: String = UUID.randomUUID().toString(),
    val attendanceRecordId: String = "",
    val workshopId: String = "",
    val workshopName: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val guardianId: String = "",
    val dateMillis: Long = 0L,
    val reason: String = "",
    val attachmentUrl: String? = null,
    val status: String = "PENDENTE", // PENDENTE, APROVADO, RECUSADO
    val requestedAt: Long = System.currentTimeMillis(),
)
