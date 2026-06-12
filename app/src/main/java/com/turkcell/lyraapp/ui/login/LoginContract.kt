package com.turkcell.lyraapp.ui.login

/**
 * Login ekranının MVI sözleşmesi.
 *
 * - [LoginUiState]: ekranın tüm görünür durumunu tutan tek kaynak (single source of truth).
 * - [LoginIntent]: kullanıcıdan gelen aksiyonlar. ViewModel bunları işleyip yeni state üretir.
 *
 * Not: Bu ekranda iş/ağ mantığı yoktur; state yalnızca saf UI durumudur.
 */
data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
)

sealed interface LoginIntent {
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data class PasswordChanged(val value: String) : LoginIntent
    data object TogglePasswordVisibility : LoginIntent
}
