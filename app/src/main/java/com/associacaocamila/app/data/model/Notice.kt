package com.associacaocamila.app.data.model

data class Notice(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "GERAL", // GERAL, RECESSO, EVENTO, URGENTE
)
