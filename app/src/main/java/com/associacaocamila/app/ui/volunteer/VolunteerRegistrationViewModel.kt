package com.associacaocamila.app.ui.volunteer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.associacaocamila.app.data.model.Volunteer
import com.associacaocamila.app.data.repository.VolunteerRepository
import com.associacaocamila.app.data.repository.VolunteerRepositoryImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VolunteerFormState(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val birthDate: String = "",
    val maritalStatus: String = "Solteiro(a)",
    val rg: String = "",
    val cpf: String = "",
    val profession: String = "",
    val address: String = "",
    val city: String = "",
    val healthLimitations: String = "",
    val supportAreas: Set<String> = emptySet(),
    val availableTimes: Set<String> = emptySet(),
    val additionalInfo: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface VolunteerRegistrationEvent {
    data class Success(val message: String) : VolunteerRegistrationEvent
    data class Error(val message: String) : VolunteerRegistrationEvent
}

class VolunteerRegistrationViewModel(
    private val repository: VolunteerRepository = VolunteerRepositoryImpl(),
) : ViewModel() {

    private val _formState = MutableStateFlow(VolunteerFormState())
    val formState: StateFlow<VolunteerFormState> = _formState.asStateFlow()

    private val _events = MutableSharedFlow<VolunteerRegistrationEvent>()
    val events: SharedFlow<VolunteerRegistrationEvent> = _events.asSharedFlow()

    fun onFullNameChange(value: String) = _formState.update { it.copy(fullName = value, errorMessage = null) }
    fun onPhoneChange(value: String) = _formState.update { it.copy(phone = value, errorMessage = null) }
    fun onEmailChange(value: String) = _formState.update { it.copy(email = value, errorMessage = null) }
    fun onBirthDateChange(value: String) = _formState.update { it.copy(birthDate = value, errorMessage = null) }
    fun onMaritalStatusChange(value: String) = _formState.update { it.copy(maritalStatus = value) }
    fun onRgChange(value: String) = _formState.update { it.copy(rg = value) }
    fun onCpfChange(value: String) = _formState.update { it.copy(cpf = value) }
    fun onProfessionChange(value: String) = _formState.update { it.copy(profession = value) }
    fun onAddressChange(value: String) = _formState.update { it.copy(address = value) }
    fun onCityChange(value: String) = _formState.update { it.copy(city = value) }
    fun onHealthLimitationsChange(value: String) = _formState.update { it.copy(healthLimitations = value) }
    fun onAdditionalInfoChange(value: String) = _formState.update { it.copy(additionalInfo = value) }

    fun toggleSupportArea(area: String) {
        _formState.update { current ->
            val updated = if (current.supportAreas.contains(area)) {
                current.supportAreas - area
            } else {
                current.supportAreas + area
            }
            current.copy(supportAreas = updated, errorMessage = null)
        }
    }

    fun toggleAvailableTime(time: String) {
        _formState.update { current ->
            val updated = if (current.availableTimes.contains(time)) {
                current.availableTimes - time
            } else {
                current.availableTimes + time
            }
            current.copy(availableTimes = updated, errorMessage = null)
        }
    }

    fun submit() {
        val state = _formState.value
        if (state.isSubmitting) return

        if (state.fullName.isBlank()) {
            _formState.update { it.copy(errorMessage = "Por favor, informe seu nome completo.") }
            return
        }
        if (state.phone.isBlank()) {
            _formState.update { it.copy(errorMessage = "Por favor, informe seu telefone/WhatsApp de contato.") }
            return
        }
        if (state.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
            _formState.update { it.copy(errorMessage = "Por favor, informe um endereço de e-mail válido.") }
            return
        }
        if (state.birthDate.isBlank()) {
            _formState.update { it.copy(errorMessage = "Por favor, informe sua data de nascimento.") }
            return
        }
        if (state.supportAreas.isEmpty()) {
            _formState.update { it.copy(errorMessage = "Selecione pelo menos uma área de apoio de seu interesse.") }
            return
        }
        if (state.availableTimes.isEmpty()) {
            _formState.update { it.copy(errorMessage = "Selecione ao menos um período de disponibilidade.") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val volunteer = Volunteer(
                fullName = state.fullName.trim(),
                phone = state.phone.trim(),
                email = state.email.trim(),
                birthDate = state.birthDate.trim(),
                maritalStatus = state.maritalStatus,
                rg = state.rg.trim(),
                cpf = state.cpf.trim(),
                profession = state.profession.trim(),
                address = state.address.trim(),
                city = state.city.trim(),
                healthLimitations = state.healthLimitations.trim(),
                supportAreas = state.supportAreas.toList(),
                availableTimes = state.availableTimes.toList(),
                additionalInfo = state.additionalInfo.trim(),
                submittedAt = System.currentTimeMillis(),
                status = "PENDENTE",
            )

            repository.submitVolunteer(volunteer)
                .onSuccess {
                    _formState.update { it.copy(isSubmitting = false) }
                    _events.emit(
                        VolunteerRegistrationEvent.Success(
                            "Inscrição enviada com sucesso! Agradecemos seu apoio e entraremos em contato."
                        )
                    )
                }
                .onFailure { error ->
                    val msg = error.localizedMessage ?: "Erro ao enviar inscrição. Tente novamente."
                    _formState.update { it.copy(isSubmitting = false, errorMessage = msg) }
                    _events.emit(VolunteerRegistrationEvent.Error(msg))
                }
        }
    }
}
