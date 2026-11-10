package com.associacaocamila.app.ui.van

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.VanBooking
import com.associacaocamila.app.data.repository.VanBookingRepository
import com.associacaocamila.app.data.repository.VanBookingRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class VanBookingUiState(
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val bookings: List<VanBooking> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isCancelling: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
)

class VanBookingViewModel(
    private val repository: VanBookingRepository = VanBookingRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(VanBookingUiState())
    val uiState: StateFlow<VanBookingUiState> = _uiState.asStateFlow()

    private var bookingsJob: Job? = null

    init {
        loadBookingsForDate(_uiState.value.selectedDateMillis)
    }

    fun selectDate(dateMillis: Long) {
        _uiState.update { it.copy(selectedDateMillis = dateMillis, errorMessage = null) }
        loadBookingsForDate(dateMillis)
    }

    fun previousDay() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.selectedDateMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        selectDate(cal.timeInMillis)
    }

    fun nextDay() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.selectedDateMillis
            add(Calendar.DAY_OF_YEAR, 1)
        }
        selectDate(cal.timeInMillis)
    }

    fun goToToday() {
        selectDate(System.currentTimeMillis())
    }

    private fun loadBookingsForDate(dateMillis: Long) {
        bookingsJob?.cancel()
        bookingsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getBookingsForDate(dateMillis)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao carregar os agendamentos da van."
                        )
                    }
                }
                .collect { bookings ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bookings = bookings
                        )
                    }
                }
        }
    }

    fun createBooking(
        destination: String,
        driverName: String,
        startHourMinute: String,
        endHourMinute: String,
        currentUser: User,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val booking = VanBooking(
                dateMillis = _uiState.value.selectedDateMillis,
                startHourMinute = startHourMinute,
                endHourMinute = endHourMinute,
                destination = destination.trim(),
                driverName = driverName.trim(),
                requestedById = currentUser.id,
                requestedByName = currentUser.name.ifBlank { "Usuário Institucional" },
                userRole = currentUser.role.name,
                status = "CONFIRMADO"
            )

            val result = repository.createBooking(booking)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        userMessage = "Agendamento da van realizado com sucesso!"
                    )
                }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Não foi possível concluir o agendamento."
                    )
                }
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, errorMessage = null) }
            val result = repository.cancelBooking(bookingId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isCancelling = false,
                        userMessage = "Reserva cancelada com sucesso."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isCancelling = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Erro ao cancelar reserva."
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(userMessage = null, errorMessage = null) }
    }
}
