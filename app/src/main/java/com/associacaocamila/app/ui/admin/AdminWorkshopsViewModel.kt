package com.associacaocamila.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.EligibleParticipant
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.Workshop
import com.associacaocamila.app.data.repository.WorkshopRepository
import com.associacaocamila.app.data.repository.WorkshopRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminWorkshopsUiState(
    val isLoadingWorkshops: Boolean = true,
    val isLoadingUsers: Boolean = false,
    val workshops: List<Workshop> = emptyList(),
    val teachers: List<User> = emptyList(),
    val students: List<User> = emptyList(),
    val eligibleParticipants: List<EligibleParticipant> = emptyList(),
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val isSeeding: Boolean = false,
    val workshopToEdit: Workshop? = null,
    val workshopToDelete: Workshop? = null,
    val userMessage: String? = null,
    val errorMessage: String? = null,
)

class AdminWorkshopsViewModel(
    private val repository: WorkshopRepository = WorkshopRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminWorkshopsUiState())
    val uiState: StateFlow<AdminWorkshopsUiState> = _uiState.asStateFlow()

    init {
        observeWorkshops()
        loadTeachersAndStudents()
    }

    private fun observeWorkshops() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWorkshops = true) }
            repository.getAllWorkshops()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingWorkshops = false,
                            errorMessage = error.localizedMessage ?: "Erro ao carregar lista de turmas.",
                        )
                    }
                }
                .collect { list ->
                    _uiState.update {
                        it.copy(
                            isLoadingWorkshops = false,
                            workshops = list,
                        )
                    }
                }
        }
    }

    fun loadTeachersAndStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUsers = true) }

            val teachersResult = repository.getApprovedTeachers()
            val studentsResult = repository.getApprovedStudents()
            val participantsResult = repository.getEligibleParticipants()

            _uiState.update { current ->
                current.copy(
                    isLoadingUsers = false,
                    teachers = teachersResult.getOrDefault(current.teachers),
                    students = studentsResult.getOrDefault(current.students),
                    eligibleParticipants = participantsResult.getOrDefault(current.eligibleParticipants),
                    errorMessage = teachersResult.exceptionOrNull()?.localizedMessage
                        ?: studentsResult.exceptionOrNull()?.localizedMessage
                        ?: participantsResult.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun selectWorkshopForEdit(workshop: Workshop?) {
        _uiState.update { it.copy(workshopToEdit = workshop) }
    }

    fun selectWorkshopForDelete(workshop: Workshop?) {
        _uiState.update { it.copy(workshopToDelete = workshop) }
    }

    fun createWorkshop(
        name: String,
        description: String,
        teacherId: String,
        selectedStudentIds: List<String>,
        onSuccess: () -> Unit = {},
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "O nome da turma não pode estar vazio.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null, userMessage = null) }

            val newWorkshop = Workshop(
                name = name.trim(),
                description = description.trim(),
                teacherId = teacherId.trim(),
                enrolledStudentIds = selectedStudentIds
            )

            repository.createWorkshop(newWorkshop)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            userMessage = "Turma criada com sucesso!"
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            errorMessage = error.localizedMessage
                                ?: "Falha ao criar turma. Tente novamente."
                        )
                    }
                }
        }
    }

    fun updateWorkshop(workshop: Workshop) {
        if (workshop.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "O nome da turma não pode estar vazio.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null, userMessage = null) }

            repository.updateWorkshop(
                workshop.copy(
                    name = workshop.name.trim(),
                    description = workshop.description.trim(),
                    teacherId = workshop.teacherId.trim(),
                )
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            workshopToEdit = null,
                            userMessage = "Oficina atualizada com sucesso"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = error.localizedMessage
                                ?: "Falha ao atualizar oficina. Tente novamente."
                        )
                    }
                }
        }
    }

    fun deleteWorkshop(workshopId: String) {
        if (workshopId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null, userMessage = null) }

            repository.deleteWorkshop(workshopId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            workshopToDelete = null,
                            userMessage = "Oficina removida com sucesso"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = error.localizedMessage
                                ?: "Falha ao excluir oficina. Tente novamente."
                        )
                    }
                }
        }
    }

    fun seedMockWorkshops() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSeeding = true, errorMessage = null, userMessage = null) }

            repository.seedMockWorkshops()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSeeding = false,
                            userMessage = "Oficinas padrão institucionais geradas com sucesso!"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSeeding = false,
                            errorMessage = error.localizedMessage
                                ?: "Falha ao gerar dados padrão de teste."
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, userMessage = null) }
    }
}
