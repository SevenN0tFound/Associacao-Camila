package com.associacaocamila.app.data.model

data class Workshop(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val teacherId: String = "",
    val enrolledStudentIds: List<String> = emptyList(),
    val isRegistrationOpen: Boolean = true,
    val registrationDeadline: Long? = null, // timestamp opcional de encerramento em milissegundos
    val maxCapacity: Int = 0, // opcional: 0 = ilimitado
) {
    /**
     * Valida se a oficina está atualmente com inscrições abertas considerando
     * a flag [isRegistrationOpen], prazo limite [registrationDeadline] e capacidade máxima [maxCapacity].
     */
    fun isCurrentlyOpen(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        if (!isRegistrationOpen) return false
        if (registrationDeadline != null && currentTimeMillis > registrationDeadline) return false
        if (maxCapacity > 0 && enrolledStudentIds.size >= maxCapacity) return false
        return true
    }

    /**
     * Verifica se a oficina atingiu a capacidade máxima.
     */
    fun isFull(): Boolean = maxCapacity > 0 && enrolledStudentIds.size >= maxCapacity

    /**
     * Vagas restantes (null se ilimitado).
     */
    val availableSpots: Int?
        get() = if (maxCapacity > 0) (maxCapacity - enrolledStudentIds.size).coerceAtLeast(0) else null
}
