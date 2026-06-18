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
 */
data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val songCount: Int,
    val createdAt: String? = null,
    val isLiked: Boolean = false,
    val isPinned: Boolean = false,
)

/**
 * Çalma listesi detayı: liste başlık bilgileri + sıralı şarkıları.
 *
 * `GET /api/v1/playlists/{id}` (`PlaylistWithSongs`) yanıtının domain karşılığıdır. "Beğenilen
 * Şarkılar" (id = "liked") için API ucu yoktur; [songs] şimdilik şarkı API'sinden doldurulur
 * (kullanıcı kararı, §2.2). [songs] mevcut [Song] domain modelini tekrar kullanır.
 */
data class PlaylistDetail(
    val id: String,
    val name: String,
    val description: String?,
    val songs: List<Song>,
)
