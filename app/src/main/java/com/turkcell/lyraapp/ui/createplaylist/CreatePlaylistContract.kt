package com.turkcell.lyraapp.ui.createplaylist

import com.turkcell.lyraapp.data.feed.Song

/**
 * Yeni çalma listesi ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [CreatePlaylistUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [CreatePlaylistIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 * - [CreatePlaylistEffect]: tek seferlik navigasyon olayları (AGENTS.MD §4.6; Register deseni).
 *
 * "Kaydet" artık işlevseldir: liste `POST /me/playlists` ile oluşturulur, seçili şarkılar
 * `me/playlists/{id}/tracks` ile eklenir (bkz. [com.turkcell.lyraapp.data.playlist.PlaylistRepository]).
 *
 * Not (§2.2 — uydurma yok): [isPublic] ("Herkese açık") yalnızca saf UI durumudur; oluşturma
 * gövdesinde görünürlük alanı olmadığından ([name] + [description] dışında) API'ya gönderilmez.
 * [songs] "Şarkı ekle" listesi için Streaming API'den ([SongRepository]) yüklenir.
 */
data class CreatePlaylistUiState(
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val songs: List<Song> = emptyList(),
    val selectedSongIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    /** "Şarkı ekle" başlığının sağındaki "N seçili" sayacı. */
    val selectedCount: Int get() = selectedSongIds.size

    /** "Kaydet" aktif mi: ad boş olmamalı ve halihazırda kaydediliyor olmamalı. */
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
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

    /** "Kaydet": listeyi oluşturur ve seçili şarkıları ekler. */
    data object SaveClicked : CreatePlaylistIntent

    /** Şarkı listesini yeniden yükler (hata sonrası tekrar dene). */
    data object Refresh : CreatePlaylistIntent
}

/** Ekranın tek seferlik olayları (state'te tutulamayacak navigasyon). */
sealed interface CreatePlaylistEffect {
    /** Kaydetme başarılı: çağıran ekranı kapatır (geri döner). */
    data object NavigateBack : CreatePlaylistEffect
}
