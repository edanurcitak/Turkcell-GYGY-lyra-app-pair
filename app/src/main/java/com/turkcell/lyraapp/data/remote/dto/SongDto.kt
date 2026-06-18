package com.turkcell.lyraapp.data.remote.dto

import com.turkcell.lyraapp.data.feed.Song
import com.turkcell.lyraapp.data.feed.StreamUrl
import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/songs` yanıt zarfı: bir sayfalık şarkı + sonraki sayfa imleci.
 *
 * `nextCursor` sayfalama içindir; bu adımda yalnızca [data] kullanılır.
 */
@Serializable
data class SongsResponseDto(
    val data: List<SongDto> = emptyList(),
    val nextCursor: String? = null,
)

/**
 * API `Song` şeması (bkz. `docs/api/openapi.json`).
 *
 * Tüm alanlar, yanıtın eksik/değişken olması ihtimaline karşı varsayılan değerlidir.
 */
@Serializable
data class SongDto(
    val id: String,
    val title: String = "",
    val artist: String = "",
    val album: String? = null,
    val durationMs: Long = 0,
    val mimeType: String = "",
    val sizeBytes: Long = 0,
    val createdAt: String? = null,
)

/** DTO → domain [Song] dönüşümü. */
fun SongDto.toDomain(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
)

/**
 * `GET /api/v1/songs/{id}/stream-url` yanıt zarfı.
 */
@Serializable
data class StreamUrlResponseDto(
    val data: StreamUrlDto,
)

/** İmzalı akış URL'i yükü (bkz. `docs/api/openapi.json`). */
@Serializable
data class StreamUrlDto(
    val url: String,
    val expiresAt: String? = null,
    val mimeType: String = "",
)

/** DTO → domain [StreamUrl] dönüşümü. */
fun StreamUrlDto.toDomain(): StreamUrl = StreamUrl(
    url = url,
    mimeType = mimeType,
    expiresAt = expiresAt,
)
