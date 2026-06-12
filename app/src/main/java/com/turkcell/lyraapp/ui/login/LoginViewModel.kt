package com.turkcell.lyraapp.ui.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Login ekranının MVI ViewModel'i.
 *
 * Tek bir [StateFlow] üzerinden [LoginUiState] yayınlar ve gelen [LoginIntent]'leri
 * [onIntent] içinde reducer mantığıyla yeni state'e dönüştürür. Bağımlılığı yoktur;
 * boş `@Inject` constructor, Hilt grafiğine katılmayı (DI) gösterir.
 */
@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged ->
                _uiState.update { it.copy(phoneNumber = intent.value) }

            is LoginIntent.PasswordChanged ->
                _uiState.update { it.copy(password = intent.value) }

            LoginIntent.TogglePasswordVisibility ->
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
        }
    }
}
