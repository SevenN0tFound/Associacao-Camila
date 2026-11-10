package com.associacaocamila.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.Volunteer
import com.associacaocamila.app.data.repository.VolunteerRepository
import com.associacaocamila.app.data.repository.VolunteerRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class VolunteerStatusFilter(val label: String) {
    TODOS("Todos"),
    PENDENTE("Pendentes"),
    CONTATADO("Contatados"),
    APROVADO("Aprovados"),
}

data class AdminVolunteersUiState(
    val volunteers: List<Volunteer> = emptyList(),
    val filteredVolunteers: List<Volunteer> = emptyList(),
    val selectedFilter: VolunteerStatusFilter = VolunteerStatusFilter.TODOS,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isUpdatingStatus: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class AdminVolunteersViewModel(
    private val repository: VolunteerRepository = VolunteerRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminVolunteersUiState())
    val uiState: StateFlow<AdminVolunteersUiState> = _uiState.asStateFlow()

    init {
        observeVolunteers()
    }

    private fun observeVolunteers() {
        viewModelScope.launch {
            repository.getVolunteers()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Erro ao carregar lista de voluntários.",
                        )
                    }
                }
                .collect { list ->
                    _uiState.update { current ->
                        current.copy(
                            volunteers = list,
                            filteredVolunteers = filterVolunteers(list, current.selectedFilter, current.searchQuery),
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun setFilter(filter: VolunteerStatusFilter) {
        _uiState.update { current ->
            current.copy(
                selectedFilter = filter,
                filteredVolunteers = filterVolunteers(current.volunteers, filter, current.searchQuery),
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredVolunteers = filterVolunteers(current.volunteers, current.selectedFilter, query),
            )
        }
    }

    fun updateStatus(volunteerId: String, newStatus: String) {
        if (volunteerId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null, successMessage = null) }
            repository.updateVolunteerStatus(volunteerId, newStatus)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isUpdatingStatus = false,
                            successMessage = "Status do voluntário atualizado para $newStatus!",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isUpdatingStatus = false,
                            errorMessage = error.localizedMessage ?: "Erro ao atualizar status do voluntário.",
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun filterVolunteers(
        list: List<Volunteer>,
        filter: VolunteerStatusFilter,
        query: String,
    ): List<Volunteer> {
        val trimmed = query.trim()
        return list.filter { vol ->
            val matchesFilter = when (filter) {
                VolunteerStatusFilter.TODOS -> true
                VolunteerStatusFilter.PENDENTE -> vol.status.equals("PENDENTE", ignoreCase = true)
                VolunteerStatusFilter.CONTATADO -> vol.status.equals("CONTATADO", ignoreCase = true)
                VolunteerStatusFilter.APROVADO -> vol.status.equals("APROVADO", ignoreCase = true)
            }
            val matchesQuery = trimmed.isBlank() ||
                vol.fullName.contains(trimmed, ignoreCase = true) ||
                vol.phone.contains(trimmed, ignoreCase = true) ||
                vol.email.contains(trimmed, ignoreCase = true) ||
                vol.city.contains(trimmed, ignoreCase = true) ||
                vol.supportAreas.any { it.contains(trimmed, ignoreCase = true) }

            matchesFilter && matchesQuery
        }
    }
}
