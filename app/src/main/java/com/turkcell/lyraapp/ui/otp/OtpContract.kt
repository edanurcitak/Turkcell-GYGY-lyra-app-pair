package com.turkcell.lyraapp.ui.otp

/** OTP kodunun hane sayısı (UI kutuları + doğrulama eşiği). */
const val OTP_CODE_LENGTH = 6

/**
 * OTP (doğrulama kodu) ekranının MVI sözleşmesi.
 *
 * - [OtpUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [OtpIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 * - [OtpEffect]: tek seferlik navigasyon olayları (AGENTS.MD §4.6).
 *
 * Akış: kod doğrulanır; `firstTime` kullanıcı profil tamamlama ([OtpEffect.NavigateToCompleteInfo]),
 * kayıtlı kullanıcı doğrudan ana ekrana ([OtpEffect.NavigateToHome]) yönlendirilir.
 */
data class OtpUiState(
    /** Doğrulanan numara, E.164-ish ("+90…") biçiminde; üst yazıda biçimlendirilerek gösterilir. */
    val phone: String = "",
    val code: String = "",
    val isSubmitting: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface OtpIntent {
    data class CodeChanged(val value: String) : OtpIntent
    data object Verify : OtpIntent
    data object Resend : OtpIntent
}

sealed interface OtpEffect {
    data object NavigateToCompleteInfo : OtpEffect
    data object NavigateToHome : OtpEffect
}
