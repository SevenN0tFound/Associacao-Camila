package com.associacaocamila.app.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.AttendanceRecord
import com.associacaocamila.app.data.model.EligibleParticipant
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.Workshop
import com.associacaocamila.app.data.repository.AttendanceRepository
import com.associacaocamila.app.data.repository.AttendanceRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val isLoadingWorkshops: Boolean = true,
    val workshops: List<Workshop> = emptyList(),
    val selectedWorkshop: Workshop? = null,
    val isLoadingStudents: Boolean = false,
    val students: List<User> = emptyList(),
    val participants: List<EligibleParticipant> = emptyList(),
    val attendanceMap: Map<String, Boolean> = emptyMap(),
    val isSubmitting: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
)

class AttendanceViewModel(
    private val repository: AttendanceRepository = AttendanceRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    private var workshopsJob: Job? = null
    private var studentsJob: Job? = null

    fun loadWorkshops(teacherId: String) {
        if (teacherId.isBlank()) return
        workshopsJob?.cancel()
        workshopsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWorkshops = true, errorMessage = null) }
            repository.getTeacherWorkshops(teacherId)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingWorkshops = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao carregar as oficinas do professor."
                        )
                    }
                }
                .collect { workshops ->
                    _uiState.update { current ->
                        val currentSelected = current.selectedWorkshop
                        // Mantém a oficina selecionada se ela ainda existir na lista, ou seleciona a primeira
                        val selected = workshops.firstOrNull { it.id == currentSelected?.id }
                            ?: workshops.firstOrNull()

                        current.copy(
                            isLoadingWorkshops = false,
                            workshops = workshops,
                            selectedWorkshop = selected
                        )
                    }

                    _uiState.value.selectedWorkshop?.let { loadStudents(it) }
                }
        }
    }

    fun selectWorkshop(workshop: Workshop) {
        if (_uiState.value.selectedWorkshop?.id == workshop.id) return
        _uiState.update { it.copy(selectedWorkshop = workshop) }
        loadStudents(workshop)
    }

    private fun loadStudents(workshop: Workshop) {
        studentsJob?.cancel()
        studentsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingStudents = true,
                    students = emptyList(),
                    participants = emptyList(),
                    attendanceMap = emptyMap(),
                    errorMessage = null
                )
            }

            if (workshop.enrolledStudentIds.isEmpty()) {
                _uiState.update { it.copy(isLoadingStudents = false) }
                return@launch
            }

            repository.getParticipantsForWorkshop(workshop.enrolledStudentIds)
                .onSuccess { participants ->
                    // Por padrão institucional, todos os participantes iniciam com presença marcada (true)
                    val defaultAttendance = participants.associateBy({ it.id }, { true })
                    _uiState.update {
                        it.copy(
                            isLoadingStudents = false,
                            participants = participants,
                            attendanceMap = defaultAttendance
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingStudents = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao carregar os participantes matriculados."
                        )
                    }
                }
        }
    }

    fun toggleStudentAttendance(studentId: String) {
        _uiState.update { state ->
            val currentStatus = state.attendanceMap[studentId] ?: true
            state.copy(
                attendanceMap = state.attendanceMap + (studentId to !currentStatus)
            )
        }
    }

    fun setStudentAttendance(studentId: String, isPresent: Boolean) {
        _uiState.update { state ->
            state.copy(
                attendanceMap = state.attendanceMap + (studentId to isPresent)
            )
        }
    }

    fun markAll(present: Boolean) {
        _uiState.update { state ->
            val updatedMap = state.participants.associate { it.id to present }
            state.copy(attendanceMap = updatedMap)
        }
    }

    fun submitAttendance(teacherId: String, onFinished: () -> Unit = {}) {
        val currentWorkshop = _uiState.value.selectedWorkshop ?: run {
            _uiState.update { it.copy(errorMessage = "Nenhuma oficina selecionada.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, userMessage = null) }

            val record = AttendanceRecord(
                workshopId = currentWorkshop.id,
                dateMillis = System.currentTimeMillis(),
                teacherId = teacherId,
                attendanceMap = _uiState.value.attendanceMap
            )

            repository.saveAttendanceRecord(record)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            userMessage = "Lista de presença salva com sucesso!"
                        )
                    }
                    onFinished()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao salvar a lista de presença. Tente novamente."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, userMessage = null) }
    }
}
