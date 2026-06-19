package com.turkcell.lyraapp.ui.createplaylist

import com.turkcell.lyraapp.data.feed.Song

/**
 * Yeni çalma listesi ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [CreatePlaylistUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [CreatePlaylistIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 *
 * Not (§2.2 / §4.6): Çalma listesi kaydetme uç noktası yok; bu yüzden "Kaydet" işlevsizdir ve
 * bir kaydetme Effect/usecase eklenmez. [name]/[description]/[isPublic] ve [selectedSongIds]
 * yalnızca saf UI durumudur. Yalnızca [songs] Streaming API'den ([SongRepository]) yüklenir
 * ("Şarkı ekle" listesi).
 */
data class CreatePlaylistUiState(
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val songs: List<Song> = emptyList(),
    val selectedSongIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    /** "Şarkı ekle" başlığının sağındaki "N seçili" sayacı. */
    val selectedCount: Int get() = selectedSongIds.size
}

sealed interface CreatePlaylistIntent {
    /** Çalma listesi adı metni değişti. */
    data class NameChanged(val value: String) : CreatePlaylistIntent

    /** Açıklama metni değişti. */
    data class DescriptionChanged(val value: String) : CreatePlaylistIntent

    /** "Herkese açık" anahtarını açar/kapatır. */
    data object TogglePublic : CreatePlaylistIntent

    /** Bir şarkının seçim durumunu değiştirir (ekle/çıkar). */
    data class ToggleSongSelection(val songId: String) : CreatePlaylistIntent

    /** Şarkı listesini yeniden yükler (hata sonrası tekrar dene). */
    data object Refresh : CreatePlaylistIntent
}
