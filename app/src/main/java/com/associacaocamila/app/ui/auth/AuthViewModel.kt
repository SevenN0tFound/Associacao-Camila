package com.associacaocamila.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserRole
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.data.repository.AuthRepository
import com.associacaocamila.app.data.repository.AuthRepositoryImpl
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data object PendingApproval : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.signIn(email, password)
                .onSuccess { user ->
                    _uiState.value = if (user.status == UserStatus.PENDING) {
                        AuthUiState.PendingApproval
                    } else {
                        AuthUiState.Success(user)
                    }
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapErrorMessage(error))
                }
        }
    }

    fun register(name: String, email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            repository.signUp(name, email, password, role)
                .onSuccess {
                    // Regra institucional: qualquer novo cadastro entra como pendente para aprovação da ONG
                    _uiState.value = AuthUiState.PendingApproval
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(mapErrorMessage(error))
                }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun signOut() {
        repository.signOut()
        resetState()
    }

    private fun mapErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is FirebaseAuthInvalidUserException -> "Usuário não encontrado. Verifique o e-mail informado."
            is FirebaseAuthWeakPasswordException -> "A senha deve ter no mínimo 6 caracteres."
            is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha incorretos."
            is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado no sistema."
            is FirebaseNetworkException -> "Sem conexão com a internet. Verifique sua rede e tente novamente."
            else -> throwable.localizedMessage?.takeIf { it.isNotBlank() }
                ?: "Ocorreu um erro inesperado. Tente novamente."
        }
    }
}
