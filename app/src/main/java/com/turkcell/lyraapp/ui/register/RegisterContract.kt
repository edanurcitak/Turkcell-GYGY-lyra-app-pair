package com.turkcell.lyraapp.ui.register

/**
 * Register ("Bilgilerini tamamla") ekranının MVI sözleşmesi.
 *
 * - [RegisterUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [RegisterIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 * - [RegisterEffect]: tek seferlik navigasyon olayları (AGENTS.MD §4.6).
 *
 * OTP doğrulaması sonrası `firstTime` kullanıcı buraya gelir; ad/soyad/doğum tarihi
 * `me/update-informations` ile kaydedilir (profil tamamlama = kayıt adımı).
 */
data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDay: String = "",
    val birthMonth: String = "",
    val birthYear: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface RegisterIntent {
    data class FirstNameChanged(val value: String) : RegisterIntent
    data class LastNameChanged(val value: String) : RegisterIntent
    data class BirthDayChanged(val value: String) : RegisterIntent
    data class BirthMonthChanged(val value: String) : RegisterIntent
    data class BirthYearChanged(val value: String) : RegisterIntent
    data object SubmitClicked : RegisterIntent
}

sealed interface RegisterEffect {
    data object NavigateToHome : RegisterEffect
}
