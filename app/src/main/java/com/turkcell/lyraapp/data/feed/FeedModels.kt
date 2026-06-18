package com.turkcell.lyraapp.data.feed

/**
 * Şarkı domain modeli.
 *
 * Streaming API'nin `Song` şemasının (bkz. `docs/api/openapi.json`) uygulama-içi karşılığıdır.
 * Yalnızca UI'ın ihtiyaç duyduğu alanlar tutulur.
 *
 * Not (§2.2): API'da şarkı için kapak/arka plan rengi yoktur. Ham renk veri katmanında
 * tutulmaz; UI katmanı [id]'den deterministik (stabil) bir renk türetir.
 */
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Long,
)

/**
 * Oynatma için kısa ömürlü imzalı akış (stream) URL'i.
 *
 * `GET /api/v1/songs/{id}/stream-url` yanıtının domain karşılığıdır; [url] doğrudan
 * ExoPlayer'a verilir (bkz. `docs/api/openapi.json`). [expiresAt] varsayılan TTL ~300 sn.
 */
data class StreamUrl(
    val url: String,
    val mimeType: String,
    val expiresAt: String?,
)
