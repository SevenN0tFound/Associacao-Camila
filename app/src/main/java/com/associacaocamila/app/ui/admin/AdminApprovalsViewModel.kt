package com.associacaocamila.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.Dependent
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.data.repository.UserRepository
import com.associacaocamila.app.data.repository.UserRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AdminApprovalsUiState {
    data object Loading : AdminApprovalsUiState
    data class Success(
        val pendingUsers: List<User>,
        val approvedUsers: List<User> = emptyList(),
    ) : AdminApprovalsUiState
    data class Error(val message: String) : AdminApprovalsUiState
}

class AdminApprovalsViewModel(
    private val repository: UserRepository = UserRepositoryImpl(),
) : ViewModel() {

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _processingUserIds = MutableStateFlow<Set<String>>(emptySet())
    val processingUserIds: StateFlow<Set<String>> = _processingUserIds.asStateFlow()

    private val _isAddingDependent = MutableStateFlow(false)
    val isAddingDependent: StateFlow<Boolean> = _isAddingDependent.asStateFlow()

    private val _availableStudents = MutableStateFlow<List<User>>(emptyList())
    val availableStudents: StateFlow<List<User>> = _availableStudents.asStateFlow()

    private val _isLoadingStudents = MutableStateFlow(false)
    val isLoadingStudents: StateFlow<Boolean> = _isLoadingStudents.asStateFlow()

    val uiState: StateFlow<AdminApprovalsUiState> = combine(
        repository.getPendingUsers(),
        repository.getApprovedUsers(),
    ) { pending, approved ->
        AdminApprovalsUiState.Success(
            pendingUsers = pending,
            approvedUsers = approved,
        ) as AdminApprovalsUiState
    }
        .catch { throwable ->
            emit(
                AdminApprovalsUiState.Error(
                    throwable.localizedMessage ?: "Ocorreu um erro ao carregar os cadastros."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AdminApprovalsUiState.Loading,
        )

    fun approveUser(userId: String) {
        updateStatus(userId, UserStatus.APPROVED)
    }

    fun rejectUser(userId: String) {
        updateStatus(userId, UserStatus.REJECTED)
    }

    private fun updateStatus(userId: String, newStatus: UserStatus) {
        viewModelScope.launch {
            _processingUserIds.value += userId
            _actionError.value = null
            repository.updateUserStatus(userId, newStatus)
                .onSuccess {
                    if (newStatus == UserStatus.APPROVED) {
                        _feedbackMessage.value = "Usuário aprovado com sucesso!"
                    } else if (newStatus == UserStatus.REJECTED) {
                        _feedbackMessage.value = "Cadastro recusado."
                    }
                }
                .onFailure { error ->
                    _actionError.value = error.localizedMessage
                        ?: "Não foi possível atualizar o status do usuário. Tente novamente."
                }
            _processingUserIds.value -= userId
        }
    }

    fun loadAvailableStudents() {
        viewModelScope.launch {
            _isLoadingStudents.value = true
            repository.getAvailableStudents()
                .onSuccess { students ->
                    _availableStudents.value = students
                }
                .onFailure { error ->
                    _actionError.value = error.localizedMessage
                        ?: "Não foi possível carregar a lista de alunos."
                }
            _isLoadingStudents.value = false
        }
    }

    fun linkStudentAsDependent(
        guardianId: String,
        student: User,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _isAddingDependent.value = true
            _actionError.value = null
            repository.linkDependentToGuardian(guardianId, student)
                .onSuccess {
                    _feedbackMessage.value = "Participante ${student.name} vinculado com sucesso!"
                    onSuccess()
                }
                .onFailure { error ->
                    _actionError.value = error.localizedMessage
                        ?: "Não foi possível vincular o participante. Tente novamente."
                }
            _isAddingDependent.value = false
        }
    }

    fun unlinkDependent(
        guardianId: String,
        dependentId: String,
        dependentName: String = "",
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _actionError.value = null
            repository.unlinkDependent(guardianId, dependentId)
                .onSuccess {
                    _feedbackMessage.value = if (dependentName.isNotBlank()) {
                        "Participante $dependentName desvinculado com sucesso."
                    } else {
                        "Participante desvinculado com sucesso."
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _actionError.value = error.localizedMessage
                        ?: "Não foi possível desvincular o participante."
                }
        }
    }

    fun addDependent(
        userId: String,
        name: String,
        birthDate: String,
        relationship: String,
        medicalNotes: String,
        onSuccess: () -> Unit = {},
    ) {
        if (name.isBlank()) {
            _actionError.value = "Por favor, informe o nome completo do dependente."
            return
        }

        viewModelScope.launch {
            _isAddingDependent.value = true
            _actionError.value = null
            val dependent = Dependent(
                name = name.trim(),
                birthDate = birthDate.trim(),
                relationship = relationship.trim().ifBlank { "Filho(a)" },
                medicalNotes = medicalNotes.trim(),
            )
            repository.addDependentToUser(userId, dependent)
                .onSuccess {
                    _feedbackMessage.value = "Dependente ${dependent.name} vinculado com sucesso!"
                    onSuccess()
                }
                .onFailure { error ->
                    _actionError.value = error.localizedMessage
                        ?: "Não foi possível vincular o dependente. Tente novamente."
                }
            _isAddingDependent.value = false
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }
}
