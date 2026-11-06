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

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
