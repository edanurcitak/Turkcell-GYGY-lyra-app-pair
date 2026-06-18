package com.turkcell.lyraapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Login ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * [LoginUiState]'i tek bir [StateFlow] üzerinden yayınlar; gelen [LoginIntent]'leri [onIntent]
 * içinde reducer mantığıyla işler. Tek seferlik navigasyon olayları [effect] kanalıyla iletilir.
 * [AuthRepository] Hilt tarafından constructor ile enjekte edilir (DI zinciri).
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect: Flow<LoginEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged ->
                _uiState.update { it.copy(phoneNumber = intent.value, errorMessage = null) }

            is LoginIntent.PasswordChanged ->
                _uiState.update { it.copy(password = intent.value, errorMessage = null) }

            LoginIntent.TogglePasswordVisibility ->
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

            LoginIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            authRepository.login(
                phoneNumber = state.phoneNumber,
                password = state.password,
            ).onSuccess {
                _uiState.update { it.copy(isSubmitting = false) }
                _effect.send(LoginEffect.NavigateToHome)
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Giriş başarısız. Bilgileri kontrol edip tekrar dene.",
                    )
                }
            }
        }
    }
}
