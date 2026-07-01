package com.turkcell.lyraapp.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `me/playlists` (kullanıcıya ait çalma listeleri) DTO'ları (bkz. `docs/api/openapi.json` → `me`).
 *
 * Favoriler için ayrı bir API ucu yoktur; "Beğenilen Şarkılar" uygulama düzeyinde bir kullanıcı
 * çalma listesiyle temsil edilir (kullanıcı kararı, §2.2). Bu dosya o listeyi yönetmek için gereken
 * gövde/yanıt tiplerini tanımlar. Listeyi listeleme (`GET /me/playlists`) katalogla aynı zarfı
 * (`{ data: [Playlist] }`) döndürdüğünden mevcut [PlaylistsResponseDto] yeniden kullanılır.
 *
 * Mevcut DTO deseni korunur: `@Serializable` zarf, yanıt alanları varsayılan değerli.
 */

/** `POST /me/playlists` gövdesi: yeni çalma listesi (ad zorunlu, açıklama opsiyonel). */
@Serializable
data class CreatePlaylistBody(
    val name: String,
    val description: String? = null,
)

/** `POST /me/playlists` yanıt zarfı: oluşturulan tekil çalma listesi (`{ data: Playlist }`). */
@Serializable
data class PlaylistResponseDto(
    val data: PlaylistDto,
)

/** `POST /me/playlists/{id}/tracks` gövdesi: listeye eklenecek şarkının id'si. */
@Serializable
data class AddTrackBody(
    val songId: String,
)

/** `POST /me/playlists/{id}/tracks` yanıt zarfı: `{ data: { added: true } }`. */
@Serializable
data class AddTrackResponseDto(
    val data: AddTrackDataDto = AddTrackDataDto(),
)

@Serializable
data class AddTrackDataDto(
    val added: Boolean = false,
)

/** `DELETE /me/playlists/{id}/tracks/{songId}` yanıt zarfı: `{ data: { removed: true } }`. */
@Serializable
data class RemoveTrackResponseDto(
    val data: RemoveTrackDataDto = RemoveTrackDataDto(),
)

@Serializable
data class RemoveTrackDataDto(
    val removed: Boolean = false,
)
