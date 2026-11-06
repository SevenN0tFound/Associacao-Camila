package com.associacaocamila.app.data.model

data class AttendanceRecord(
    val id: String = "",
    val workshopId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val teacherId: String = "",
    val attendanceMap: Map<String, Boolean> = emptyMap(), // Map de userId para present (true/false)
)
