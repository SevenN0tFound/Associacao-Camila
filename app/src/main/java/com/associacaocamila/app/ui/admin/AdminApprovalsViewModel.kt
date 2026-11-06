package com.associacaocamila.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.User
import com.associacaocamila.app.data.model.UserStatus
import com.associacaocamila.app.data.repository.UserRepository
import com.associacaocamila.app.data.repository.UserRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AdminApprovalsUiState {
    data object Loading : AdminApprovalsUiState
    data class Success(val pendingUsers: List<User>) : AdminApprovalsUiState
    data class Error(val message: String) : AdminApprovalsUiState
}

class AdminApprovalsViewModel(
    private val repository: UserRepository = UserRepositoryImpl(),
) : ViewModel() {

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _processingUserIds = MutableStateFlow<Set<String>>(emptySet())
    val processingUserIds: StateFlow<Set<String>> = _processingUserIds.asStateFlow()

    val uiState: StateFlow<AdminApprovalsUiState> = repository.getPendingUsers()
        .map<List<User>, AdminApprovalsUiState> { users ->
            AdminApprovalsUiState.Success(users)
        }
        .catch { throwable ->
            emit(
                AdminApprovalsUiState.Error(
                    throwable.localizedMessage ?: "Ocorreu um erro ao carregar os cadastros pendentes."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AdminApprovalsUiState.Loading
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
                .onFailure { error ->
                    _actionError.value = error.localizedMessage
                        ?: "Não foi possível atualizar o status do usuário. Tente novamente."
                }
            _processingUserIds.value -= userId
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }
}
