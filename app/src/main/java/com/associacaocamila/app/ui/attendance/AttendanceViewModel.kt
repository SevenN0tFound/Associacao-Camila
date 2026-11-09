package com.associacaocamila.app.ui.attendance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.AttendanceRecord
import com.associacaocamila.app.data.model.AttendanceStatus
import com.associacaocamila.app.data.model.EligibleParticipant
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.Workshop
import com.associacaocamila.app.data.repository.AttendanceRepository
import com.associacaocamila.app.data.repository.AttendanceRepositoryImpl
import com.associacaocamila.app.util.NotificationHelper
import com.associacaocamila.app.util.WorkshopReportPdfGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class AttendanceUiState(
    val isLoadingWorkshops: Boolean = true,
    val workshops: List<Workshop> = emptyList(),
    val selectedWorkshop: Workshop? = null,
    val isLoadingStudents: Boolean = false,
    val students: List<User> = emptyList(),
    val participants: List<EligibleParticipant> = emptyList(),
    val attendanceMap: Map<String, Boolean> = emptyMap(),
    val statusMap: Map<String, AttendanceStatus> = emptyMap(),
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val isReadOnly: Boolean = false,
    val isDateHasRecord: Boolean = false,
    val currentRecord: AttendanceRecord? = null,
    val workshopRecords: List<AttendanceRecord> = emptyList(),
    val isSubmitting: Boolean = false,
    val isExportingPdf: Boolean = false,
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
                        val selected = workshops.firstOrNull { it.id == currentSelected?.id }
                            ?: workshops.firstOrNull()

                        current.copy(
                            isLoadingWorkshops = false,
                            workshops = workshops,
                            selectedWorkshop = selected
                        )
                    }

                    _uiState.value.selectedWorkshop?.let { loadStudentsAndRecords(it) }
                }
        }
    }

    fun selectWorkshop(workshop: Workshop) {
        if (_uiState.value.selectedWorkshop?.id == workshop.id) return
        _uiState.update { it.copy(selectedWorkshop = workshop) }
        loadStudentsAndRecords(workshop)
    }

    private fun loadStudentsAndRecords(workshop: Workshop) {
        studentsJob?.cancel()
        studentsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingStudents = true,
                    students = emptyList(),
                    participants = emptyList(),
                    attendanceMap = emptyMap(),
                    statusMap = emptyMap(),
                    errorMessage = null
                )
            }

            if (workshop.enrolledStudentIds.isEmpty()) {
                _uiState.update { it.copy(isLoadingStudents = false) }
                return@launch
            }

            // 1. Carrega participantes matriculados
            val participantsResult = repository.getParticipantsForWorkshop(workshop.enrolledStudentIds)
            val participants = participantsResult.getOrElse { error ->
                _uiState.update {
                    it.copy(
                        isLoadingStudents = false,
                        errorMessage = error.localizedMessage ?: "Erro ao carregar os participantes matriculados."
                    )
                }
                return@launch
            }

            // 2. Carrega histórico de chamadas da oficina
            val recordsResult = repository.getAttendanceRecordsForWorkshop(workshop.id)
            val records = recordsResult.getOrDefault(emptyList())

            _uiState.update {
                it.copy(
                    isLoadingStudents = false,
                    participants = participants,
                    workshopRecords = records
                )
            }

            syncAttendanceForSelectedDate()
        }
    }

    fun previousDay() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.selectedDateMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        selectDate(cal.timeInMillis)
    }

    fun nextDay() {
        val currentMillis = _uiState.value.selectedDateMillis
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            timeInMillis = currentMillis
            add(Calendar.DAY_OF_YEAR, 1)
        }
        // Não permite avançar para dias futuros
        if (cal.after(now) && !isSameDay(cal.timeInMillis, now.timeInMillis)) {
            return
        }
        selectDate(cal.timeInMillis)
    }

    fun selectDate(dateMillis: Long) {
        _uiState.update { it.copy(selectedDateMillis = dateMillis) }
        syncAttendanceForSelectedDate()
    }

    fun isToday(millis: Long): Boolean = isSameDay(millis, System.currentTimeMillis())

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return c1.get(Calendar.ERA) == c2.get(Calendar.ERA) &&
                c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun syncAttendanceForSelectedDate() {
        val state = _uiState.value
        val dateMillis = state.selectedDateMillis
        val isTodayDate = isToday(dateMillis)
        val records = state.workshopRecords
        val matchingRecord = records.firstOrNull { isSameDay(it.dateMillis, dateMillis) }

        if (isTodayDate) {
            if (matchingRecord != null) {
                val statusMap = state.participants.associate { p ->
                    p.id to matchingRecord.getParticipantStatus(p.id)
                }
                val attendanceMap = state.participants.associate { p ->
                    p.id to (matchingRecord.attendanceMap[p.id] ?: (statusMap[p.id] == AttendanceStatus.PRESENTE))
                }
                _uiState.update {
                    it.copy(
                        isReadOnly = false,
                        isDateHasRecord = true,
                        currentRecord = matchingRecord,
                        attendanceMap = attendanceMap,
                        statusMap = statusMap
                    )
                }
            } else {
                val defaultAttendance = state.participants.associate { it.id to true }
                val defaultStatuses = state.participants.associate { it.id to AttendanceStatus.PRESENTE }
                _uiState.update {
                    it.copy(
                        isReadOnly = false,
                        isDateHasRecord = false,
                        currentRecord = null,
                        attendanceMap = defaultAttendance,
                        statusMap = defaultStatuses
                    )
                }
            }
        } else {
            // Data anterior: MODO READ-ONLY (Trava de edição)
            if (matchingRecord != null) {
                val statusMap = state.participants.associate { p ->
                    p.id to matchingRecord.getParticipantStatus(p.id)
                }
                val attendanceMap = state.participants.associate { p ->
                    p.id to (matchingRecord.attendanceMap[p.id] ?: (statusMap[p.id] == AttendanceStatus.PRESENTE))
                }
                _uiState.update {
                    it.copy(
                        isReadOnly = true,
                        isDateHasRecord = true,
                        currentRecord = matchingRecord,
                        attendanceMap = attendanceMap,
                        statusMap = statusMap
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isReadOnly = true,
                        isDateHasRecord = false,
                        currentRecord = null,
                        attendanceMap = emptyMap(),
                        statusMap = emptyMap()
                    )
                }
            }
        }
    }

    fun setStudentAttendance(studentId: String, isPresent: Boolean) {
        if (_uiState.value.isReadOnly) return
        _uiState.update { state ->
            val newStatus = if (isPresent) AttendanceStatus.PRESENTE else AttendanceStatus.FALTA
            state.copy(
                attendanceMap = state.attendanceMap + (studentId to isPresent),
                statusMap = state.statusMap + (studentId to newStatus)
            )
        }
    }

    fun toggleStudentAttendance(studentId: String) {
        if (_uiState.value.isReadOnly) return
        val currentStatus = _uiState.value.attendanceMap[studentId] ?: true
        setStudentAttendance(studentId, !currentStatus)
    }

    fun markAll(present: Boolean) {
        if (_uiState.value.isReadOnly) return
        _uiState.update { state ->
            val updatedMap = state.participants.associate { it.id to present }
            val newStatus = if (present) AttendanceStatus.PRESENTE else AttendanceStatus.FALTA
            val updatedStatusMap = state.participants.associate { it.id to newStatus }
            state.copy(attendanceMap = updatedMap, statusMap = updatedStatusMap)
        }
    }

    fun submitAttendance(
        teacherId: String,
        context: Context? = null,
        onFinished: () -> Unit = {},
    ) {
        if (_uiState.value.isReadOnly) {
            _uiState.update { it.copy(errorMessage = "Registros de datas anteriores são somente para leitura.") }
            return
        }

        val currentWorkshop = _uiState.value.selectedWorkshop ?: run {
            _uiState.update { it.copy(errorMessage = "Nenhuma oficina selecionada.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, userMessage = null) }

            val recordId = _uiState.value.currentRecord?.id?.ifBlank { "" } ?: ""
            val currentStatuses = _uiState.value.statusMap
            val statusMapToSave = _uiState.value.attendanceMap.mapValues { (id, isPresent) ->
                val existing = currentStatuses[id]
                if (existing == AttendanceStatus.JUSTIFICADO) {
                    AttendanceStatus.JUSTIFICADO.name
                } else if (isPresent) {
                    AttendanceStatus.PRESENTE.name
                } else {
                    AttendanceStatus.FALTA.name
                }
            }

            val record = AttendanceRecord(
                id = recordId,
                workshopId = currentWorkshop.id,
                dateMillis = _uiState.value.selectedDateMillis,
                teacherId = teacherId,
                attendanceMap = _uiState.value.attendanceMap,
                statusMap = statusMapToSave
            )

            repository.saveAttendanceRecord(record)
                .onSuccess {
                    val updatedRecords = _uiState.value.workshopRecords.filter { it.id != record.id } + record
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            currentRecord = record,
                            isDateHasRecord = true,
                            workshopRecords = updatedRecords,
                            userMessage = "Lista de presença salva com sucesso!"
                        )
                    }

                    // Dispara alerta local para cada participante que recebeu falta
                    if (context != null) {
                        val absentIds = record.attendanceMap.filterValues { !it }.keys
                        val participantMap = _uiState.value.participants.associateBy({ it.id }, { it.name })

                        absentIds.forEach { absentId ->
                            val studentName = participantMap[absentId] ?: "Participante"
                            NotificationHelper.sendAbsenceAlert(
                                context = context,
                                studentName = studentName,
                                workshopName = currentWorkshop.name,
                            )
                        }
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

    fun exportAttendancePdf(
        context: Context,
        teacherName: String = "Professor(a)",
    ) {
        val workshop = _uiState.value.selectedWorkshop ?: run {
            _uiState.update { it.copy(errorMessage = "Selecione uma oficina para exportar o relatório.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExportingPdf = true, errorMessage = null) }
            repository.getAttendanceRecordsForWorkshop(workshop.id)
                .onSuccess { records ->
                    runCatching {
                        val studentsMap = _uiState.value.participants.associateBy({ it.id }, { it.name })
                        val file = WorkshopReportPdfGenerator.generateAttendancePdf(
                            context = context,
                            workshop = workshop,
                            records = records,
                            studentsMap = studentsMap,
                            teacherName = teacherName,
                        )
                        _uiState.update { it.copy(isExportingPdf = false) }
                        WorkshopReportPdfGenerator.sharePdf(context, file)
                    }.onFailure { ex ->
                        _uiState.update {
                            it.copy(
                                isExportingPdf = false,
                                errorMessage = "Erro ao gerar PDF: ${ex.localizedMessage}",
                            )
                        }
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isExportingPdf = false,
                            errorMessage = err.localizedMessage ?: "Erro ao buscar histórico de chamadas.",
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, userMessage = null) }
    }
}
