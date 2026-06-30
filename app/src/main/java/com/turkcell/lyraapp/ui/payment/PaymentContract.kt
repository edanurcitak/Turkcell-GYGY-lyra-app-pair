package com.turkcell.lyraapp.ui.payment

import com.turkcell.lyraapp.data.membership.MembershipPlan

/**
 * Ödeme (checkout) ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [PaymentUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [PaymentIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 * - [PaymentEffect]: tek seferlik navigasyon olayı (AGENTS.MD §4.6) — ödeme onayında profile dönüş.
 *
 * [plan] GERÇEK API'den gelir: nav ile taşınan `planId`, [com.turkcell.lyraapp.data.membership.
 * MembershipRepository.getPlans] içinden çözülür; ad/fiyat uydurulmaz (§2.2). Kart alanları yalnızca
 * rakam tutar (gösterimde gruplanır); ödeme `memberships/checkout` ile alınır.
 */
data class PaymentUiState(
    /** Satın alınacak plan; planId API'den çözülene dek `null` (yükleniyor). */
    val plan: MembershipPlan? = null,
    /** Kart numarası — yalnızca rakam, en çok 16 hane (gösterimde 4'erli gruplanır). */
    val cardNumber: String = "",
    val holderName: String = "",
    /** Son kullanma — yalnızca rakam "MMYY", en çok 4 hane (gösterimde "AA/YY"). */
    val expiry: String = "",
    /** CVC — yalnızca rakam, 3–4 hane. */
    val cvc: String = "",
    /** Plan yükleniyor (açılış). */
    val isLoading: Boolean = true,
    /** Checkout isteği sürüyor (ödeme butonu spinner'a döner, çift gönderim engellenir). */
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
) {
    /** Plan yüklü + tüm kart alanları geçerli + işlem yokken ödeme yapılabilir. */
    val canPay: Boolean
        get() = plan != null &&
            !isProcessing &&
            cardNumber.length == 16 &&
            holderName.isNotBlank() &&
            expiry.length == 4 &&
            cvc.length in 3..4
}

sealed interface PaymentIntent {
    data class CardNumberChanged(val value: String) : PaymentIntent
    data class HolderNameChanged(val value: String) : PaymentIntent
    data class ExpiryChanged(val value: String) : PaymentIntent
    data class CvcChanged(val value: String) : PaymentIntent

    /** "… öde" butonu: checkout'u tetikler. */
    data object PayClicked : PaymentIntent

    /** Plan yükleme hatası sonrası "Tekrar dene". */
    data object Retry : PaymentIntent
}

sealed interface PaymentEffect {
    /** Ödeme onaylandı; profile dönülür (banner premium'a döner, premium özellikler açılır). */
    data object PaymentApproved : PaymentEffect
}
