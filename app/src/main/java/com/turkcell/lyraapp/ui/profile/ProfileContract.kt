package com.turkcell.lyraapp.ui.profile

/**
 * Profil ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [ProfileUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [ProfileIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 *
 * Not: Profil/istatistik ve ayar satırları statik içeriktir (ekran görüntüsü referansı;
 * API karşılığı yok, §2.2). Ekranın tek işlevsel etkileşimi "Görünüm" tema seçimidir
 * ([ProfileIntent.ThemeChanged]); diğer satırlar yalnızca tasarımdır, aksiyon tetiklemez (§4.6).
 */
data class ProfileUiState(
    val initials: String = "ZK",
    val displayName: String = "Zeynep Kaya",
    val handle: String = "@zeynepk",
    val membership: String = "Premium",
    val playlistCount: String = "127",
    val followerCount: String = "1.2B",
    val followingCount: String = "348",
    val isDarkTheme: Boolean = false,
    val settings: List<ProfileSetting> = defaultProfileSettings,
)

sealed interface ProfileIntent {
    /** Görünüm toggle'ı: açık/koyu tema seçimi (uygulama geneline uygulanır). */
    data class ThemeChanged(val darkTheme: Boolean) : ProfileIntent
}

/**
 * Ayarlar listesindeki tek bir satırın saf modeli.
 *
 * İkon tutmaz: ikon, çizim katmanında [id] üzerinden temadan çözülür ([SearchGenre] ile
 * aynı mantık) — `ImageVector` yalnızca `@Composable` bağlamında okunur.
 */
data class ProfileSetting(
    val id: String,
    val title: String,
    val value: String? = null,
)

/** Ayarlar listesinin statik içeriği (ekran görüntüsü referansı). */
val defaultProfileSettings: List<ProfileSetting> = listOf(
    ProfileSetting(id = "audio_quality", title = "Ses kalitesi", value = "Yüksek"),
    ProfileSetting(id = "offline_download", title = "Çevrimdışı indirme", value = "Açık"),
    ProfileSetting(id = "notifications", title = "Bildirimler"),
    ProfileSetting(id = "privacy", title = "Gizlilik"),
    ProfileSetting(id = "help", title = "Yardım ve destek"),
)
