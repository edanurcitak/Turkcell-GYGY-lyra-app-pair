package com.turkcell.lyraapp.ui.register

/**
 * Register (Hesap oluştur) ekranının MVI sözleşmesi.
 *
 * - [RegisterUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [RegisterIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 *
 * Not: İş/ağ/validation mantığı yoktur; state yalnızca saf UI durumudur (AGENTS.MD §4.6).
 */
data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isTermsAccepted: Boolean = false,
    val isSubmitting: Boolean = false,
)

sealed interface RegisterIntent {
    data class FirstNameChanged(val value: String) : RegisterIntent
    data class LastNameChanged(val value: String) : RegisterIntent
    data class PhoneNumberChanged(val value: String) : RegisterIntent
    data class PasswordChanged(val value: String) : RegisterIntent
    data object TogglePasswordVisibility : RegisterIntent
    data object ToggleTermsAccepted : RegisterIntent
    data object SubmitClicked : RegisterIntent
}
