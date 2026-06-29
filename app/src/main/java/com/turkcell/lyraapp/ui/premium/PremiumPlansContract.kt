package com.turkcell.lyraapp.ui.premium

import com.turkcell.lyraapp.data.membership.MembershipPlan

/**
 * Premium plan seçim ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [PremiumPlansUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [PremiumPlansIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 *
 * [plans] GERÇEK API'den gelir (`GET /api/v1/memberships/plans`,
 * [com.turkcell.lyraapp.data.membership.MembershipRepository]); fiyat/ad/açıklama uydurulmaz (§2.2).
 * [features] ise ekran tasarımının pazarlama metnidir (API karşılığı yok); statik tutulur — tıpkı
 * [com.turkcell.lyraapp.ui.profile.defaultProfileSettings] gibi.
 */
data class PremiumPlansUiState(
    val features: List<PremiumFeature> = defaultPremiumFeatures,
    val plans: List<MembershipPlan> = emptyList(),
    /** Seçili planın id'si; planlar yüklenince ViewModel "recurring" (aylık) plana ayarlar. */
    val selectedPlanId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface PremiumPlansIntent {
    /** Bir plan kartına dokunuldu (radio seçimi). */
    data class PlanSelected(val planId: String) : PremiumPlansIntent

    /** Yükleme hatası sonrası "Tekrar dene". */
    data object Retry : PremiumPlansIntent
}

/**
 * Premium özellik satırının saf modeli (ekran tasarımı kopyası; API karşılığı yok, §2.2).
 *
 * İkon tutmaz: çizim katmanında [id] üzerinden temadan/ikon setinden çözülür
 * ([com.turkcell.lyraapp.ui.profile.ProfileSetting] ile aynı mantık).
 */
data class PremiumFeature(
    val id: String,
    val title: String,
    val subtitle: String,
)

/** Özellik listesinin statik içeriği (ekran görüntüsü referansı). */
val defaultPremiumFeatures: List<PremiumFeature> = listOf(
    PremiumFeature(id = "no_ads", title = "Reklamsız dinleme", subtitle = "Kesintisiz, sınırsız müzik"),
    PremiumFeature(id = "unlimited_skip", title = "Sınırsız atlama", subtitle = "İstediğin şarkıya geç"),
    PremiumFeature(id = "offline", title = "Çevrimdışı indirme", subtitle = "İnternet olmadan dinle"),
    PremiumFeature(id = "high_quality", title = "Yüksek ses kalitesi", subtitle = "320 kbps net ses"),
    PremiumFeature(id = "all_devices", title = "Tüm cihazlarında", subtitle = "Telefon, tablet ve masaüstü"),
)
