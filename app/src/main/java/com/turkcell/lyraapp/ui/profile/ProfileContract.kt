package com.turkcell.lyraapp.ui.profile

/**
 * Profil ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [ProfileUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [ProfileIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 *
 * Not: İstatistik ve ayar satırları statik içeriktir (ekran görüntüsü referansı; API karşılığı
 * yok, §2.2). İşlevsel etkileşimler: "Görünüm" tema seçimi ([ProfileIntent.ThemeChanged]) ve free
 * banner tıklaması (navigasyon; intent değil, ekran callback'i ile — projenin nav kalıbı). Diğer
 * satırlar yalnızca tasarımdır, aksiyon tetiklemez (§4.6).
 *
 * [displayName] / [initials]: oturum açan kullanıcının register'da girdiği ad/soyaddan türetilir;
 * ViewModel bunları [com.turkcell.lyraapp.data.auth.UserStore] üzerinden besler. Buradaki
 * varsayılanlar yalnızca preview/boş-durum içindir (canlı değerler depodan gelir).
 *
 * [isPremium]: tier kaynağı API'dir (istemci hesaplamaz, §2.2); ViewModel bunu
 * [com.turkcell.lyraapp.data.membership.MembershipStore] üzerinden türetir. Banner ve header alt
 * satırı ([membership]) buna göre değişir (premium → "Premium · 3 gün", free → "Ücretsiz").
 *
 * [premiumDaysRemaining]: premium ise `Membership.expiresAt`'ten türetilen kalan tam gün (kaynak
 * API; §2.2 — istemci tier'ı hesaplamaz, yalnızca bitiş tarihinden görünüm türetir). Banner son 3
 * güne kadar "Premium üyelik", son 3 günde "Premium · {n} gün kaldı" gösterir. null → normal metin.
 */
data class ProfileUiState(
    val initials: String = "LK",
    val displayName: String = "Lyra Kullanıcısı",
    val isPremium: Boolean = true,
    val membership: String = "Premium · 3 gün",
    val premiumDaysRemaining: Int? = null,
    val playlistCount: String = "127",
    val followerCount: String = "1.2B",
    val followingCount: String = "348",
    val isDarkTheme: Boolean = false,
    val settings: List<ProfileSetting> = defaultProfileSettings,
)

sealed interface ProfileIntent {
    /** Görünüm toggle'ı: açık/koyu tema seçimi (uygulama geneline uygulanır). */
    data class ThemeChanged(val darkTheme: Boolean) : ProfileIntent

    /** "Çıkış yap": oturumu (token/tier/kimlik) temizler ve login'e döner. */
    data object Logout : ProfileIntent
}

/**
 * Profil ekranının tek seferlik (one-shot) yan etkileri (AGENTS.MD §4.6 — navigasyon açıkça talep
 * edildiğinde eklenir; [com.turkcell.lyraapp.ui.login.LoginEffect] ile aynı Channel kalıbı).
 */
sealed interface ProfileEffect {
    /** Çıkış tamamlandı; login ekranına dön (geri yığın temizlenerek). */
    data object NavigateToLogin : ProfileEffect
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
