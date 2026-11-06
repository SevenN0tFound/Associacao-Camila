package com.associacaocamila.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.repository.StudentPortalRepository
import com.associacaocamila.app.data.repository.StudentPortalRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentAttendanceRecordItem(
    val id: String,
    val workshopId: String,
    val dateMillis: Long,
    val isPresent: Boolean,
)

data class StudentAttendanceUiState(
    val isLoading: Boolean = true,
    val history: List<StudentAttendanceRecordItem> = emptyList(),
    val totalLessons: Int = 0,
    val totalPresences: Int = 0,
    val totalAbsences: Int = 0,
    val attendancePercentage: Float = 100f,
    val isRegular: Boolean = true,
    val errorMessage: String? = null,
)

class StudentAttendanceViewModel(
    private val repository: StudentPortalRepository = StudentPortalRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAttendanceUiState())
    val uiState: StateFlow<StudentAttendanceUiState> = _uiState.asStateFlow()

    private var attendanceJob: Job? = null

    fun loadAttendance(studentId: String) {
        if (studentId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, history = emptyList()) }
            return
        }

        attendanceJob?.cancel()
        attendanceJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getStudentAttendanceHistory(studentId)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao carregar o histórico de frequência."
                        )
                    }
                }
                .collect { historyList ->
                    val items = historyList.map { (record, isPresent) ->
                        StudentAttendanceRecordItem(
                            id = record.id,
                            workshopId = record.workshopId,
                            dateMillis = record.dateMillis,
                            isPresent = isPresent,
                        )
                    }

                    val total = items.size
                    val presences = items.count { it.isPresent }
                    val absences = total - presences
                    val percentage = if (total > 0) (presences.toFloat() / total) * 100f else 100f
                    val isRegular = percentage >= 75f

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            history = items,
                            totalLessons = total,
                            totalPresences = presences,
                            totalAbsences = absences,
                            attendancePercentage = percentage,
                            isRegular = isRegular,
                        )
                    }
                }
        }
    }
}
