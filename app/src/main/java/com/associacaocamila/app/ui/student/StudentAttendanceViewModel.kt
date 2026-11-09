package com.associacaocamila.app.ui.student

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.AbsenceJustification
import com.associacaocamila.app.data.model.AttendanceStatus
import com.associacaocamila.app.data.repository.JustificationRepository
import com.associacaocamila.app.data.repository.JustificationRepositoryImpl
import com.associacaocamila.app.data.repository.StudentPortalRepository
import com.associacaocamila.app.data.repository.StudentPortalRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentAttendanceRecordItem(
    val id: String,
    val workshopId: String,
    val workshopName: String = "",
    val dateMillis: Long,
    val isPresent: Boolean,
    val status: AttendanceStatus = AttendanceStatus.PRESENTE,
    val justification: AbsenceJustification? = null,
)

data class StudentAttendanceUiState(
    val isLoading: Boolean = true,
    val history: List<StudentAttendanceRecordItem> = emptyList(),
    val totalLessons: Int = 0,
    val totalPresences: Int = 0,
    val totalAbsences: Int = 0,
    val totalJustified: Int = 0,
    val attendancePercentage: Float = 100f,
    val isRegular: Boolean = true,
    val isSubmittingJustification: Boolean = false,
    val errorMessage: String? = null,
)

class StudentAttendanceViewModel(
    private val portalRepository: StudentPortalRepository = StudentPortalRepositoryImpl(),
    private val justificationRepository: JustificationRepository = JustificationRepositoryImpl(),
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

            combine(
                portalRepository.getStudentAttendanceHistory(studentId),
                portalRepository.getStudentWorkshops(studentId),
                justificationRepository.getJustificationsForStudent(studentId)
            ) { historyList, workshops, justifications ->
                Triple(historyList, workshops, justifications)
            }.catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Erro ao carregar o histórico de frequência."
                    )
                }
            }.collect { (historyList, workshops, justifications) ->
                val workshopsMap = workshops.associateBy({ it.id }, { it.name })
                val justificationsByRecord = justifications.associateBy { it.attendanceRecordId }

                val items = historyList.map { (record, isPresent) ->
                    val workshopName = workshopsMap[record.workshopId] ?: "Oficina"
                    val justification = justificationsByRecord[record.id]

                    val effectiveStatus = when {
                        record.getParticipantStatus(studentId) == AttendanceStatus.JUSTIFICADO ||
                                justification?.status == "APROVADO" -> AttendanceStatus.JUSTIFICADO
                        isPresent -> AttendanceStatus.PRESENTE
                        else -> AttendanceStatus.FALTA
                    }

                    StudentAttendanceRecordItem(
                        id = record.id,
                        workshopId = record.workshopId,
                        workshopName = workshopName,
                        dateMillis = record.dateMillis,
                        isPresent = isPresent || effectiveStatus == AttendanceStatus.JUSTIFICADO,
                        status = effectiveStatus,
                        justification = justification
                    )
                }

                val total = items.size
                val presences = items.count { it.status == AttendanceStatus.PRESENTE }
                val justified = items.count { it.status == AttendanceStatus.JUSTIFICADO }
                val absences = items.count { it.status == AttendanceStatus.FALTA }

                // Na assiduidade institucional, faltas justificadas são abonadas (contam positivamente para regularidade)
                val effectivePositive = presences + justified
                val percentage = if (total > 0) (effectivePositive.toFloat() / total) * 100f else 100f
                val isRegular = percentage >= 75f

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        history = items,
                        totalLessons = total,
                        totalPresences = presences,
                        totalJustified = justified,
                        totalAbsences = absences,
                        attendancePercentage = percentage,
                        isRegular = isRegular,
                    )
                }
            }
        }
    }

    fun submitJustification(
        justification: AbsenceJustification,
        fileUri: Uri?,
        mimeType: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingJustification = true) }
            justificationRepository.submitJustification(justification, fileUri, mimeType)
                .onSuccess {
                    _uiState.update { it.copy(isSubmittingJustification = false) }
                    onSuccess()
                }
                .onFailure { ex ->
                    _uiState.update { it.copy(isSubmittingJustification = false) }
                    onError(ex.localizedMessage ?: "Erro ao enviar justificativa. Tente novamente.")
                }
        }
    }
}
