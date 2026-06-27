package com.turkcell.lyraapp.data.playback

import com.turkcell.lyraapp.data.auth.TokenStore
import com.turkcell.lyraapp.data.remote.MeApi
import com.turkcell.lyraapp.data.remote.dto.AdCompleteBody
import com.turkcell.lyraapp.data.remote.dto.PlaybackNextBody
import com.turkcell.lyraapp.data.remote.dto.PlaybackNextDataDto
import com.turkcell.lyraapp.data.remote.dto.toDomain
import javax.inject.Inject

/**
 * Free akış oynatma kararının domain arayüzü (SongRepository/PlaylistRepository deseni).
 *
 * Premium doğrudan `stream-url` kullanır; **free** ise sunucu-otoriteli `playback/next` ile her
 * şarkıyı çözer (reklam enjeksiyonu + çalma kaydı sunucu tarafında). Tek implementasyon
 * [ApiPlaybackRepository]'dir.
 *
 * Not (§2.1): Arayüz ve implementasyonu, batch'in 5 dosya sınırını aşmamak için aynı dosyada tutuldu.
 */
interface PlaybackRepository {

    /** Sıradaki çalınabilir öğeyi çözer: doğrudan şarkı ya da önce reklam. */
    suspend fun resolveNext(songId: String): PlaybackResolution

    /** Tamamlanan reklamı sunucuya bildirir (analytics; en iyi çaba). */
    suspend fun completeAd(impressionId: String)
}

/**
 * [PlaybackRepository]'nin `me/playback` grubu implementasyonu.
 *
 * Korumalı uçlardır: Bearer token [TokenStore]'dan okunur ve [MeApi]'ye `@Header` ile geçirilir
 * (mevcut [com.turkcell.lyraapp.data.feed.ApiSongRepository] deseni).
 */
class ApiPlaybackRepository @Inject constructor(
    private val meApi: MeApi,
    private val tokenStore: TokenStore,
) : PlaybackRepository {

    override suspend fun resolveNext(songId: String): PlaybackResolution =
        meApi.playbackNext(bearer(), PlaybackNextBody(songId)).data.toResolution()

    override suspend fun completeAd(impressionId: String) {
        meApi.adComplete(bearer(), AdCompleteBody(impressionId))
    }

    private fun bearer(): String {
        val token = tokenStore.accessToken
            ?: throw IllegalStateException("Oturum yok (erişim token'ı bulunamadı).")
        return "Bearer $token"
    }
}

/** DTO → domain [PlaybackResolution]; eksik/uyumsuz yük halinde hata fırlatır (§2.2 — uydurmaz). */
private fun PlaybackNextDataDto.toResolution(): PlaybackResolution {
    val resolvedSong = song?.toDomain() ?: error("playback/next: şarkı bilgisi eksik.")
    val resolvedStream = stream?.toDomain() ?: error("playback/next: şarkı akış URL'i eksik.")
    return if (type.equals("ad", ignoreCase = true)) {
        val adDto = ad ?: error("playback/next: reklam bilgisi eksik (type=ad).")
        val adStreamUrl = adStream?.toDomain() ?: error("playback/next: reklam akış URL'i eksik (type=ad).")
        val impression = impressionId ?: error("playback/next: impressionId eksik (type=ad).")
        PlaybackResolution.WithAd(
            song = resolvedSong,
            streamUrl = resolvedStream,
            ad = AdInfo(
                id = adDto.id,
                title = adDto.title,
                advertiser = adDto.advertiser,
                durationMs = adDto.durationMs,
            ),
            adStreamUrl = adStreamUrl,
            impressionId = impression,
        )
    } else {
        PlaybackResolution.SongOnly(song = resolvedSong, streamUrl = resolvedStream)
    }
}
