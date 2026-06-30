package com.turkcell.lyraapp.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `memberships/checkout` (premium satın alma — mock kart ödemesi) DTO'ları
 * (bkz. `docs/api/openapi.json` → `memberships/checkout`).
 *
 * Mevcut DTO deseni korunur: `@Serializable` istek gövdesi + `@Serializable` yanıt zarfı
 * (`...ResponseDto { data }`), yanıt alanları varsayılan değerli (eksik/değişken yanıta karşı).
 *
 * Sonuç **kart numarasına** göre belirlenir (mock): `4242 4242 4242 4242` → onay (201);
 * `4000 0000 0000 0002` ve diğerleri → red (402). Yanıttaki aktif üyelik için `AuthDto.kt`'deki
 * [MembershipDto] + [toDomain] yeniden kullanılır (satın alınan üyeliğin tier'ı oradan okunur).
 */

// --- İstek gövdesi ---

/** `POST /api/v1/memberships/checkout` gövdesi. */
@Serializable
data class CheckoutBody(
    /** "one-time" | "recurring" — satın alınacak planın türü ([MembershipPlanDto.type]). */
    val plan: String,
    val card: CardDto,
)

/** Mock kart bilgileri (gerçek ödeme sağlayıcısı yok; sonuç [number]'a göre belirlenir). */
@Serializable
data class CardDto(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String? = null,
)

// --- Yanıt zarfı ---

/** `POST /api/v1/memberships/checkout` yanıt zarfı (201 — ödeme onaylandı). */
@Serializable
data class CheckoutResponseDto(
    val data: CheckoutDataDto = CheckoutDataDto(),
)

/** Checkout yükü: işlem (payment) + aktivasyonla oluşan aktif [MembershipDto]. */
@Serializable
data class CheckoutDataDto(
    val payment: PaymentDto? = null,
    val membership: MembershipDto? = null,
)

/** Mock ödeme işlemi özeti. */
@Serializable
data class PaymentDto(
    val transactionId: String = "",
    val amountKurus: Int = 0,
    val currency: String = "TRY",
)
