package com.associacaocamila.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.Workshop
import com.associacaocamila.app.data.repository.StudentPortalRepository
import com.associacaocamila.app.data.repository.StudentPortalRepositoryImpl
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
    val errorMessage: String? = null,
)

class StudentWorkshopsViewModel(
    private val repository: StudentPortalRepository = StudentPortalRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentWorkshopsUiState())
    val uiState: StateFlow<StudentWorkshopsUiState> = _uiState.asStateFlow()

    private var workshopsJob: Job? = null

    fun loadWorkshops(studentId: String) {
        if (studentId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, workshops = emptyList()) }
            return
        }

        workshopsJob?.cancel()
        workshopsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getStudentWorkshops(studentId)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao carregar as suas oficinas matriculadas."
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
}
