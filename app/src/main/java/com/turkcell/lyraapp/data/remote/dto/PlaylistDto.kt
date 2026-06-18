package com.turkcell.lyraapp.data.remote.dto

import com.turkcell.lyraapp.data.playlist.Playlist
import com.turkcell.lyraapp.data.playlist.PlaylistDetail
import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/playlists` yanıt zarfı: tüm çalma listeleri (şarkısız).
 */
@Serializable
data class PlaylistsResponseDto(
    val data: List<PlaylistDto> = emptyList(),
)

/**
 * API `Playlist` şeması (bkz. `docs/api/openapi.json`).
 *
 * Liste ucu şarkı içermez; şarkı sayısı detay ucundan ([PlaylistDetailDto.songs]) sayılır.
 * Tüm alanlar, yanıtın eksik/değişken olması ihtimaline karşı varsayılan değerlidir.
 */
@Serializable
data class PlaylistDto(
    val id: String,
    val name: String = "",
    val description: String? = null,
    val createdAt: String? = null,
)

/**
 * `GET /api/v1/playlists/{id}` yanıt zarfı: çalma listesi + sıralı şarkıları.
 */
@Serializable
data class PlaylistDetailResponseDto(
    val data: PlaylistDetailDto,
)

/** API `PlaylistWithSongs` şeması (Playlist + songs[]). */
@Serializable
data class PlaylistDetailDto(
    val id: String,
    val name: String = "",
    val description: String? = null,
    val createdAt: String? = null,
    val songs: List<SongDto> = emptyList(),
)

/** DTO → domain [PlaylistDetail] dönüşümü (şarkılar [SongDto.toDomain] ile çevrilir). */
fun PlaylistDetailDto.toDomain(): PlaylistDetail = PlaylistDetail(
    id = id,
    name = name,
    description = description,
    songs = songs.map { it.toDomain() },
)

/**
 * DTO → domain [Playlist] dönüşümü.
 *
 * [songCount] liste ucunda gelmediğinden çağıran tarafça (repository) detay ucundan sayılıp
 * verilir.
 */
fun PlaylistDto.toDomain(songCount: Int): Playlist = Playlist(
    id = id,
    name = name,
    description = description,
    songCount = songCount,
    createdAt = createdAt,
)
