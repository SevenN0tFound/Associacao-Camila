package com.associacaocamila.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.repository.UserRepository
import com.associacaocamila.app.data.repository.UserRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val availableStudents: List<User> = emptyList(),
    val isLoadingStudents: Boolean = false,
    val isLinkingDependent: Boolean = false,
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateProfile(
        currentUser: User,
        phone: String,
        birthDate: String,
        address: String,
        medicalNotes: String,
        onSuccess: (User) -> Unit = {},
    ) {
        val trimmedPhone = phone.trim()
        val trimmedBirthDate = birthDate.trim()
        val trimmedAddress = address.trim()
        val trimmedMedicalNotes = medicalNotes.trim()

        val updates = mapOf<String, Any>(
            "phone" to trimmedPhone,
            "birthDate" to trimmedBirthDate,
            "address" to trimmedAddress,
            "medicalNotes" to trimmedMedicalNotes,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val result = userRepository.updateUserProfile(currentUser.id, updates)
            result.onSuccess {
                val updatedUser = currentUser.copy(
                    phone = trimmedPhone,
                    birthDate = trimmedBirthDate,
                    address = trimmedAddress,
                    medicalNotes = trimmedMedicalNotes,
                )
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Perfil atualizado com sucesso!",
                    )
                }
                onSuccess(updatedUser)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.localizedMessage ?: "Erro ao salvar alterações no perfil.",
                    )
                }
            }
        }
    }

    fun loadAvailableStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStudents = true) }
            userRepository.getAvailableStudents()
                .onSuccess { students ->
                    _uiState.update {
                        it.copy(
                            isLoadingStudents = false,
                            availableStudents = students,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingStudents = false,
                            errorMessage = error.localizedMessage
                                ?: "Não foi possível carregar a lista de alunos.",
                        )
                    }
                }
        }
    }

    fun linkStudentAsDependent(
        guardian: User,
        student: User,
        currentUser: User? = null,
        onSuccess: (User) -> Unit = {},
    ) {
        val requesterRole = currentUser?.role?.uppercase() ?: guardian.role.uppercase()
        if (requesterRole != "ADMIN") {
            _uiState.update {
                it.copy(
                    isLinkingDependent = false,
                    errorMessage = "Acesso negado: apenas administradores podem vincular dependentes.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLinkingDependent = true, errorMessage = null, successMessage = null) }
            userRepository.linkDependentToGuardian(guardian.id, student, requesterRole)
                .onSuccess {
                    val newDependent = com.associacaocamila.app.data.model.Dependent(
                        id = student.id,
                        name = student.name,
                        birthDate = student.birthDate,
                        relationship = "Aluno(a) / Dependente",
                        medicalNotes = student.medicalNotes,
                        documents = student.documents,
                    )
                    val updatedUser = guardian.copy(
                        dependents = guardian.dependents + newDependent,
                        associatedStudentIds = guardian.associatedStudentIds + student.id,
                    )
                    _uiState.update {
                        it.copy(
                            isLinkingDependent = false,
                            successMessage = "Participante ${student.name} vinculado com sucesso!",
                        )
                    }
                    onSuccess(updatedUser)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLinkingDependent = false,
                            errorMessage = error.localizedMessage
                                ?: "Não foi possível vincular o participante. Tente novamente.",
                        )
                    }
                }
        }
    }

    fun unlinkDependent(
        guardian: User,
        dependentId: String,
        dependentName: String = "",
        currentUser: User? = null,
        onSuccess: (User) -> Unit = {},
    ) {
        val requesterRole = currentUser?.role?.uppercase() ?: guardian.role.uppercase()
        if (requesterRole != "ADMIN") {
            _uiState.update {
                it.copy(
                    errorMessage = "Acesso negado: apenas administradores podem desvincular dependentes.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, successMessage = null) }
            userRepository.unlinkDependent(guardian.id, dependentId, requesterRole)
                .onSuccess {
                    val updatedUser = guardian.copy(
                        dependents = guardian.dependents.filterNot { it.id == dependentId },
                        associatedStudentIds = guardian.associatedStudentIds.filterNot { it == dependentId },
                    )
                    _uiState.update {
                        it.copy(
                            successMessage = if (dependentName.isNotBlank()) {
                                "Participante $dependentName desvinculado com sucesso."
                            } else {
                                "Participante desvinculado com sucesso."
                            },
                        )
                    }
                    onSuccess(updatedUser)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.localizedMessage
                                ?: "Não foi possível desvincular o participante.",
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
