package com.turkcell.lyraapp.ui.feed

import com.turkcell.lyraapp.data.feed.Song

/**
 * Ana sayfa (feed) ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [FeedUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [FeedIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 *
 * Üç içerik bölümü ayrı korumalı uçlardan ([com.turkcell.lyraapp.data.feed.SongRepository])
 * yüklenir:
 * - [recommendations] → `GET /me/recommendations` ("Önerilenler" — üstteki ızgara)
 * - [recentlyPlayed]  → `GET /me/recently-played` ("Son çalınanlar")
 * - [forYou]          → `GET /me/for-you` ("Senin için müzikler")
 *
 * Not: [greeting] şarkı verisi değildir (API'da karşılığı yok); statik varsayılan kalır.
 * [userInitials] oturum açan kullanıcının ad/soyadından türetilir (ViewModel, app-scoped
 * [com.turkcell.lyraapp.data.auth.UserStore]'dan aynalar; kaynak API, §2.2 — istemci uydurmaz).
 * Buradaki "ZK" yalnızca preview/boş-durum fallback'idir. [isDarkTheme] app-scoped tema durumunu
 * yansıtır (başlıktaki tema düğmesi).
 */
data class FeedUiState(
    val greeting: String = "İyi akşamlar",
    val userInitials: String = "ZK",
    val recommendations: List<Song> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val forYou: List<Song> = emptyList(),
    val isDarkTheme: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface FeedIntent {
    /** İçeriği yeniden yükle (ilk yükleme, ON_RESUME tazeleme ve hata sonrası "Tekrar dene"). */
    data object Refresh : FeedIntent

    /**
     * Kullanıcının aşağı çekme (pull-to-refresh) hareketi. [Refresh] ile aynı yüklemeyi yapar ancak
     * üstteki dönen göstergeyi besler ([FeedUiState.isRefreshing]); ON_RESUME tazelemesi sessizdir.
     */
    data object PullRefresh : FeedIntent

    /** Başlıktaki tema düğmesi: açık/koyu temaya geç. */
    data class ToggleTheme(val darkTheme: Boolean) : FeedIntent
}
