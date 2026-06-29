package com.turkcell.lyraapp.data.remote.dto

import com.turkcell.lyraapp.data.membership.MembershipPlan
import kotlinx.serialization.Serializable

/**
 * `memberships/plans` (premium fiyat kataloğu) DTO'ları (bkz. `docs/api/openapi.json` → `MembershipPlan`).
 *
 * Mevcut DTO deseni korunur: `@Serializable` zarf (`...ResponseDto { data }`), tüm alanlar
 * varsayılan değerli (yanıtın eksik/değişken olmasına karşı), DTO → domain `toDomain()` ile.
 *
 * Not: Buradaki [MembershipPlanDto] (satın alınabilir plan kataloğu), `AuthDto.kt`'deki
 * [MembershipDto]'dan (kullanıcının aktif üyelik durumu) ayrıdır.
 */

/** `GET /api/v1/memberships/plans` yanıt zarfı. */
@Serializable
data class MembershipPlansResponseDto(
    val data: List<MembershipPlanDto> = emptyList(),
)

/** API `MembershipPlan` şeması. */
@Serializable
data class MembershipPlanDto(
    val id: String = "",
    /** "one-time" | "recurring". */
    val type: String = "",
    val name: String = "",
    val description: String = "",
    /** Fiyat kuruş cinsinden (1₺ = 100 kuruş). */
    val priceKurus: Int = 0,
    /** Fiyat tam lira cinsinden (ör. 139). */
    val price: Int = 0,
    val currency: String = "TRY",
    val durationDays: Int = 0,
    val autoRenew: Boolean = false,
)

/** DTO → domain [MembershipPlan]. */
fun MembershipPlanDto.toDomain(): MembershipPlan = MembershipPlan(
    id = id,
    type = type,
    name = name,
    description = description,
    priceKurus = priceKurus,
    currency = currency,
    durationDays = durationDays,
    autoRenew = autoRenew,
)
