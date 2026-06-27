package com.turkcell.lyraapp.data.playback

import com.turkcell.lyraapp.data.feed.Song
import com.turkcell.lyraapp.data.feed.StreamUrl

/** Bir reklamın oynatılması için gereken bilgi (API `Ad` şeması — bkz. `docs/api/openapi.json`). */
data class AdInfo(
    val id: String,
    val title: String,
    val advertiser: String,
    val durationMs: Long,
)

/**
 * `POST /me/playback/next` sonucunun domain karşılığı (free akış).
 *
 * Sunucu "sıradaki ne çalınmalı?"yı belirler: doğrudan şarkı ([SongOnly]) ya da önce reklam
 * ([WithAd]). Her iki durumda da çalınacak [song] + onun [streamUrl]'i bulunur; reklamlıda ek
 * olarak reklamın akışı ([WithAd.adStreamUrl]) ve tamamlanınca bildirilecek [WithAd.impressionId]
 * vardır.
 */
sealed interface PlaybackResolution {
    val song: Song
    val streamUrl: StreamUrl

    data class SongOnly(
        override val song: Song,
        override val streamUrl: StreamUrl,
    ) : PlaybackResolution

    data class WithAd(
        override val song: Song,
        override val streamUrl: StreamUrl,
        val ad: AdInfo,
        val adStreamUrl: StreamUrl,
        val impressionId: String,
    ) : PlaybackResolution
}
