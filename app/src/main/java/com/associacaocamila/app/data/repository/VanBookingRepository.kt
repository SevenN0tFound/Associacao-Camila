package com.associacaocamila.app.data.repository

import com.associacaocamila.app.data.model.VanBooking
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

private const val VAN_BOOKINGS_COLLECTION = "van_bookings"

interface VanBookingRepository {
    /**
     * Busca em tempo real as reservas da van ativas para o dia selecionado.
     */
    fun getBookingsForDate(dateMillis: Long): Flow<List<VanBooking>>

    /**
     * Cria um novo agendamento validando a prevenção de conflitos de horário.
     */
    suspend fun createBooking(booking: VanBooking): Result<Unit>

    /**
     * Cancela uma reserva existente atualizando seu status para "CANCELADO".
     */
    suspend fun cancelBooking(bookingId: String): Result<Unit>
}

class VanBookingRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : VanBookingRepository {

    private val bookingsCollection get() = firestore.collection(VAN_BOOKINGS_COLLECTION)

    override fun getBookingsForDate(dateMillis: Long): Flow<List<VanBooking>> = callbackFlow {
        val (startOfDay, endOfDay) = getDayBounds(dateMillis)
        val query = bookingsCollection
            .whereGreaterThanOrEqualTo("dateMillis", startOfDay)
            .whereLessThanOrEqualTo("dateMillis", endOfDay)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val bookings = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(VanBooking::class.java)?.copy(id = doc.id)
            }.orEmpty()
                .filter { it.status == "CONFIRMADO" }
                .sortedBy { parseMinutes(it.startHourMinute) }

            trySend(bookings)
        }

        awaitClose { registration.remove() }
    }

    override suspend fun createBooking(booking: VanBooking): Result<Unit> = runCatching {
        if (booking.destination.isBlank()) {
            throw IllegalArgumentException("Por favor, informe o destino ou finalidade da reserva.")
        }
        if (booking.driverName.isBlank()) {
            throw IllegalArgumentException("Por favor, informe o nome do condutor responsável.")
        }
        if (booking.startHourMinute.isBlank() || booking.endHourMinute.isBlank()) {
            throw IllegalArgumentException("Por favor, preencha os horários de início e término.")
        }

        val newStart = parseMinutes(booking.startHourMinute)
        val newEnd = parseMinutes(booking.endHourMinute)
        if (newEnd <= newStart) {
            throw IllegalArgumentException("O horário de término deve ser posterior ao horário de início.")
        }

        val effectiveDate = if (booking.dateMillis > 0L) booking.dateMillis else System.currentTimeMillis()
        val (startOfDay, endOfDay) = getDayBounds(effectiveDate)

        // Consulta as reservas do dia para verificação anti-conflito
        val snapshot = bookingsCollection
            .whereGreaterThanOrEqualTo("dateMillis", startOfDay)
            .whereLessThanOrEqualTo("dateMillis", endOfDay)
            .get()
            .await()

        val existingBookings = snapshot.documents.mapNotNull { doc ->
            doc.toObject(VanBooking::class.java)?.copy(id = doc.id)
        }

        // Regra anti-conflito: (novoInicio < existenteFim && novoFim > existenteInicio) com status "CONFIRMADO"
        val conflito = existingBookings.firstOrNull { existing ->
            if (existing.id == booking.id) return@firstOrNull false
            if (existing.status != "CONFIRMADO") return@firstOrNull false
            val existingStart = parseMinutes(existing.startHourMinute)
            val existingEnd = parseMinutes(existing.endHourMinute)
            (newStart < existingEnd && newEnd > existingStart)
        }

        if (conflito != null) {
            throw Exception("A van já está reservada nesse horário por ${conflito.requestedByName}")
        }

        val bookingToSave = booking.copy(
            dateMillis = startOfDay,
            status = "CONFIRMADO"
        )

        bookingsCollection
            .document(bookingToSave.id)
            .set(bookingToSave)
            .await()
    }

    override suspend fun cancelBooking(bookingId: String): Result<Unit> = runCatching {
        if (bookingId.isBlank()) {
            throw IllegalArgumentException("ID da reserva inválido.")
        }
        bookingsCollection
            .document(bookingId)
            .update("status", "CANCELADO")
            .await()
    }

    companion object {
        /**
         * Converte uma string no formato "HH:mm" em minutos decorridos no dia.
         * Ex: "08:30" -> 8 * 60 + 30 = 510.
         */
        fun parseMinutes(hourMinute: String): Int {
            val parts = hourMinute.trim().split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            return h * 60 + m
        }

        /**
         * Retorna os limites inicial (00:00:00.000) e final (23:59:59.999) do dia
         * no fuso horário do dispositivo.
         */
        fun getDayBounds(dateMillis: Long): Pair<Long, Long> {
            val cal = Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis
            return Pair(start, end)
        }
    }
}
