package com.associacaocamila.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.Announcement
import com.associacaocamila.app.data.repository.AnnouncementRepository
import com.associacaocamila.app.data.repository.AnnouncementRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoadingAnnouncements: Boolean = true,
    val announcements: List<Announcement> = emptyList(),
    val isPostingAnnouncement: Boolean = false,
    val isDeletingAnnouncement: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val announcementRepository: AnnouncementRepository = AnnouncementRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeAnnouncements()
    }

    fun observeAnnouncements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAnnouncements = true) }
            announcementRepository.getAnnouncements()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingAnnouncements = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao carregar o feed de comunicados.",
                        )
                    }
                }
                .collect { list ->
                    _uiState.update {
                        it.copy(
                            isLoadingAnnouncements = false,
                            announcements = list,
                        )
                    }
                }
        }
    }

    fun createAnnouncement(
        title: String,
        message: String,
        category: String,
        authorName: String = "Coordenação",
        onSuccess: () -> Unit = {},
    ) {
        if (title.isBlank() || message.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Título e mensagem são obrigatórios.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPostingAnnouncement = true, errorMessage = null, userMessage = null) }

            val newAnnouncement = Announcement(
                title = title.trim(),
                message = message.trim(),
                category = category.trim().ifBlank { "Geral" },
                authorName = authorName.trim().ifBlank { "Coordenação" },
                timestamp = System.currentTimeMillis(),
            )

            announcementRepository.postAnnouncement(newAnnouncement)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isPostingAnnouncement = false,
                            userMessage = "Comunicado publicado com sucesso!",
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPostingAnnouncement = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao publicar comunicado. Tente novamente.",
                        )
                    }
                }
        }
    }

    fun removeAnnouncement(id: String) {
        if (id.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAnnouncement = true, errorMessage = null, userMessage = null) }

            announcementRepository.deleteAnnouncement(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeletingAnnouncement = false,
                            userMessage = "Comunicado removido com sucesso!",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDeletingAnnouncement = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao remover comunicado. Tente novamente.",
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, userMessage = null) }
    }
}
