package com.turkcell.lyraapp.ui.login

/**
 * Login ekranının MVI sözleşmesi.
 *
 * - [LoginUiState]: ekranın tüm görünür durumunu tutan tek kaynak (single source of truth).
 * - [LoginIntent]: kullanıcıdan gelen aksiyonlar. ViewModel bunları işleyip yeni state üretir.
 * - [LoginEffect]: tek seferlik (one-shot) yan etkiler; navigasyon gibi (AGENTS.MD §4.6).
 */
data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface LoginIntent {
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data class PasswordChanged(val value: String) : LoginIntent
    data object TogglePasswordVisibility : LoginIntent
    data object Submit : LoginIntent
}

sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
}
