package com.associacaocamila.app.data.model

import java.util.UUID

data class Announcement(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val message: String = "",
    val category: String = "Geral", // Geral, Oficinas, Reunião, Acolhimento
    val authorName: String = "Coordenação",
    val timestamp: Long = System.currentTimeMillis(),
)
