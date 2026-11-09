package com.associacaocamila.app.data.model

data class AttendanceRecord(
    val id: String = "",
    val workshopId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val teacherId: String = "",
    val attendanceMap: Map<String, Boolean> = emptyMap(), // Map de userId para present (true/false)
    val statusMap: Map<String, String> = emptyMap(), // Map de userId para AttendanceStatus (PRESENTE, FALTA, JUSTIFICADO)
) {
    fun getParticipantStatus(participantId: String): AttendanceStatus {
        val statusString = statusMap[participantId]
        if (statusString != null) {
            return runCatching { AttendanceStatus.valueOf(statusString) }.getOrDefault(
                if (attendanceMap[participantId] == true) AttendanceStatus.PRESENTE else AttendanceStatus.FALTA
            )
        }
        return if (attendanceMap[participantId] == true) AttendanceStatus.PRESENTE else AttendanceStatus.FALTA
    }
}
