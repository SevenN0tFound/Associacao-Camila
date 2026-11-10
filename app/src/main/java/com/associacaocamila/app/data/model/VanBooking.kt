package com.associacaocamila.app.data.model

import java.util.UUID

/**
 * Modelo representativo de uma reserva da van institucional.
 */
data class VanBooking(
    val id: String = UUID.randomUUID().toString(),
    val dateMillis: Long = 0L,          // Dia da reserva
    val startHourMinute: String = "",   // ex: "08:00"
    val endHourMinute: String = "",     // ex: "10:30"
    val destination: String = "",       // Destino / Atividade
    val driverName: String = "",        // Condutor / Responsável
    val requestedById: String = "",
    val requestedByName: String = "",
    val userRole: String = "",
    val status: String = "CONFIRMADO",  // CONFIRMADO, CANCELADO
    val createdAt: Long = System.currentTimeMillis()
)
