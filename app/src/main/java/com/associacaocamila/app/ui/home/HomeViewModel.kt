package com.associacaocamila.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.Notice
import com.associacaocamila.app.data.repository.NoticeRepository
import com.associacaocamila.app.data.repository.NoticeRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoadingNotices: Boolean = true,
    val notices: List<Notice> = emptyList(),
    val isPostingNotice: Boolean = false,
    val isDeletingNotice: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val noticeRepository: NoticeRepository = NoticeRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeNotices()
    }

    fun observeNotices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNotices = true) }
            noticeRepository.getNotices()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingNotices = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao carregar o mural de recados e avisos.",
                        )
                    }
                }
                .collect { list ->
                    _uiState.update {
                        it.copy(
                            isLoadingNotices = false,
                            notices = list,
                        )
                    }
                }
        }
    }

    fun addNotice(
        title: String,
        content: String,
        category: String,
        authorName: String,
        authorRole: String = "",
        onSuccess: () -> Unit = {},
    ) {
        if (title.isBlank() || content.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Título e conteúdo são obrigatórios.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPostingNotice = true, errorMessage = null, userMessage = null) }

            val newNotice = Notice(
                title = title.trim(),
                content = content.trim(),
                category = category.trim().ifBlank { "GERAL" }.uppercase(),
                authorName = authorName.trim().ifBlank { "Coordenação" },
                authorRole = authorRole.trim(),
                timestamp = System.currentTimeMillis(),
            )

            noticeRepository.addNotice(newNotice)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isPostingNotice = false,
                            userMessage = "Aviso publicado no mural com sucesso!",
                        )
                    }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isPostingNotice = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao publicar aviso. Tente novamente.",
                        )
                    }
                }
        }
    }

    fun deleteNotice(id: String) {
        if (id.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingNotice = true, errorMessage = null, userMessage = null) }

            noticeRepository.deleteNotice(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeletingNotice = false,
                            userMessage = "Aviso removido do mural com sucesso!",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDeletingNotice = false,
                            errorMessage = error.localizedMessage
                                ?: "Erro ao remover aviso do mural. Tente novamente.",
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, userMessage = null) }
    }
}
