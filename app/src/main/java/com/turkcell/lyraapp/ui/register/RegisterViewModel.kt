package com.turkcell.lyraapp.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Register ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * Tek bir [StateFlow] üzerinden [RegisterUiState] yayınlar; gelen [RegisterIntent]'leri
 * [onIntent] içinde reducer mantığıyla yeni state'e dönüştürür. [AuthRepository] Hilt
 * tarafından constructor üzerinden enjekte edilir (DI zinciri).
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.FirstNameChanged ->
                _uiState.update { it.copy(firstName = intent.value) }

            is RegisterIntent.LastNameChanged ->
                _uiState.update { it.copy(lastName = intent.value) }

            is RegisterIntent.PhoneNumberChanged ->
                _uiState.update { it.copy(phoneNumber = intent.value) }

            is RegisterIntent.PasswordChanged ->
                _uiState.update { it.copy(password = intent.value) }

            RegisterIntent.TogglePasswordVisibility ->
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

            RegisterIntent.ToggleTermsAccepted ->
                _uiState.update { it.copy(isTermsAccepted = !it.isTermsAccepted) }

            RegisterIntent.SubmitClicked -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            authRepository.register(
                firstName = state.firstName,
                lastName = state.lastName,
                phoneNumber = state.phoneNumber,
                password = state.password,
            )
            // Not: Başarı/hata sonrası akış (navigasyon, snackbar) henüz kapsam dışı.
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }
}
