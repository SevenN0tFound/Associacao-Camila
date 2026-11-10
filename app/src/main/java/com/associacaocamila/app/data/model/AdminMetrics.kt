package com.associacaocamila.app.data.model

data class AdminMetrics(
    val totalStudents: Int = 0,
    val activeWorkshops: Int = 0,
    val pendingVolunteers: Int = 0,
    val overallAttendanceRate: Int = 0, // percentual de 0 a 100
)
