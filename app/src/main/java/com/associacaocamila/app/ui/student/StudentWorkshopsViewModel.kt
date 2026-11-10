package com.associacaocamila.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.Workshop
import com.associacaocamila.app.data.repository.WorkshopRepository
import com.associacaocamila.app.data.repository.WorkshopRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentWorkshopsUiState(
    val isLoading: Boolean = true,
    val workshops: List<Workshop> = emptyList(),
    val isPerformingAction: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
)

class StudentWorkshopsViewModel(
    private val workshopRepository: WorkshopRepository = WorkshopRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentWorkshopsUiState())
    val uiState: StateFlow<StudentWorkshopsUiState> = _uiState.asStateFlow()

    private var workshopsJob: Job? = null

    init {
        loadAllWorkshops()
    }

    fun loadAllWorkshops() {
        workshopsJob?.cancel()
        workshopsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            workshopRepository.getAllWorkshops()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao carregar a grade de oficinas."
                        )
                    }
                }
                .collect { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            workshops = list
                        )
                    }
                }
        }
    }

    /**
     * Mantém compatibilidade com chamadas existentes passando o studentId.
     */
    fun loadWorkshops(@Suppress("UNUSED_PARAMETER") studentId: String = "") {
        if (workshopsJob == null || !workshopsJob!!.isActive) {
            loadAllWorkshops()
        }
    }

    fun enrollStudent(workshopId: String, studentId: String, studentName: String? = null) {
        if (workshopId.isBlank() || studentId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true, errorMessage = null, userMessage = null) }
            workshopRepository.enrollStudent(workshopId, studentId)
                .onSuccess {
                    val msg = if (!studentName.isNullOrBlank()) {
                        "Inscrição de $studentName realizada com sucesso!"
                    } else {
                        "Inscrição realizada com sucesso!"
                    }
                    _uiState.update { it.copy(isPerformingAction = false, userMessage = msg) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPerformingAction = false,
                            errorMessage = error.localizedMessage ?: "Falha ao realizar inscrição."
                        )
                    }
                }
        }
    }

    fun unrollStudent(workshopId: String, studentId: String, studentName: String? = null) {
        if (workshopId.isBlank() || studentId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true, errorMessage = null, userMessage = null) }
            workshopRepository.unrollStudent(workshopId, studentId)
                .onSuccess {
                    val msg = if (!studentName.isNullOrBlank()) {
                        "Inscrição de $studentName cancelada."
                    } else {
                        "Inscrição cancelada com sucesso."
                    }
                    _uiState.update { it.copy(isPerformingAction = false, userMessage = msg) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPerformingAction = false,
                            errorMessage = error.localizedMessage ?: "Falha ao cancelar inscrição."
                        )
                    }
                }
        }
    }

    fun updateDependentEnrollments(
        workshopId: String,
        selectedDependentIds: Set<String>,
        currentEnrolledDependentIds: Set<String>,
        onComplete: () -> Unit = {},
    ) {
        if (workshopId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPerformingAction = true, errorMessage = null, userMessage = null) }
            val toAdd = selectedDependentIds - currentEnrolledDependentIds
            val toRemove = currentEnrolledDependentIds - selectedDependentIds

            var hasError = false
            var lastErrorMessage: String? = null

            for (depId in toAdd) {
                val res = workshopRepository.enrollStudent(workshopId, depId)
                if (res.isFailure) {
                    hasError = true
                    lastErrorMessage = res.exceptionOrNull()?.localizedMessage
                }
            }

            for (depId in toRemove) {
                val res = workshopRepository.unrollStudent(workshopId, depId)
                if (res.isFailure) {
                    hasError = true
                    lastErrorMessage = res.exceptionOrNull()?.localizedMessage
                }
            }

            if (hasError) {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        errorMessage = lastErrorMessage ?: "Erro ao atualizar inscrições dos dependentes."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isPerformingAction = false,
                        userMessage = "Inscrições dos dependentes atualizadas com sucesso!"
                    )
                }
                onComplete()
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(userMessage = null, errorMessage = null) }
    }
}
