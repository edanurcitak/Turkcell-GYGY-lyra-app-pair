package com.turkcell.lyraapp.data.playlist

import com.turkcell.lyraapp.data.feed.Song

/**
 * Çalma listesi domain modeli.
 *
 * Streaming API'nin `Playlist` şemasının (bkz. `docs/api/openapi.json`) uygulama-içi
 * karşılığıdır. Yalnızca UI'ın ihtiyaç duyduğu alanlar tutulur.
 *
 * Not (§2.2): [songCount] liste ucunda gelmez ("without songs"); detay ucundan
 * (`/playlists/{id}`) türetilir. [isLiked]/[isPinned] API'da yoktur — "Beğenilen Şarkılar"
 * uygulama düzeyinde özel bir listedir ve sayısı şimdilik şarkı API'sinden türetilir
 * (kullanıcı kararı, geçici). Kapak rengi de API'da yoktur; UI katmanı temadan türetir.
 *
 * [isOwned]: liste kullanıcının kendi çalma listesi mi (`GET /me/playlists`'ten gelen). Yalnızca
 * owned listeler `me/playlists` grubu uçlarıyla düzenlenip silinebilir; öne çıkan (public) listeler
 * ve "Beğenilenler"/"İndirilenler" özel listeleri düzenlenemez.
 */
data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val songCount: Int,
    val createdAt: String? = null,
    val isLiked: Boolean = false,
    val isPinned: Boolean = false,
    val isOwned: Boolean = false,
)

/**
 * Çalma listesi detayı: liste başlık bilgileri + sıralı şarkıları.
 *
 * `GET /api/v1/playlists/{id}` (`PlaylistWithSongs`) yanıtının domain karşılığıdır. "Beğenilen
 * Şarkılar" (id = "liked") için API ucu yoktur; [songs] şimdilik şarkı API'sinden doldurulur
 * (kullanıcı kararı, §2.2). [songs] mevcut [Song] domain modelini tekrar kullanır.
 *
 * [isOwned]: liste kullanıcıya mı ait (yanıt `ownerId` alanı dolu). Yalnızca owned listelerde
 * şarkı ekleme/çıkarma UI'ı gösterilir (bkz. [Playlist.isOwned]).
 */
data class PlaylistDetail(
    val id: String,
    val name: String,
    val description: String?,
    val songs: List<Song>,
    val isOwned: Boolean = false,
)
