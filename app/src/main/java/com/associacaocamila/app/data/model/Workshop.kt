package com.associacaocamila.app.data.model

data class Workshop(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val teacherId: String = "",
    val enrolledStudentIds: List<String> = emptyList(),
)
