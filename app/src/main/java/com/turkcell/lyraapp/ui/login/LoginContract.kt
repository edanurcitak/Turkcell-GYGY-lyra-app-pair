package com.turkcell.lyraapp.ui.login

/**
 * Login ekranının MVI sözleşmesi.
 *
 * - [LoginUiState]: ekranın tüm görünür durumunu tutan tek kaynak (single source of truth).
 * - [LoginIntent]: kullanıcıdan gelen aksiyonlar. ViewModel bunları işleyip yeni state üretir.
 * - [LoginEffect]: tek seferlik (one-shot) yan etkiler; navigasyon gibi (AGENTS.MD §4.6).
 *
 * Parolasız akış: kullanıcı yalnızca telefon numarası girer. "Devam et" → OTP istenir ve
 * doğrulama ekranına geçilir; kayıtlı/kayıtsız dallanması OTP doğrulamasından sonra yapılır.
 */
data class LoginUiState(
    val phoneNumber: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface LoginIntent {
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data object Submit : LoginIntent
}

sealed interface LoginEffect {
    /** OTP "gönderildi"; doğrulama ekranına geç. [phone] E.164-ish ("+90…") biçimindedir. */
    data class NavigateToOtp(val phone: String) : LoginEffect
}
