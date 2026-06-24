package com.turkcell.lyraapp.data.feed

import com.turkcell.lyraapp.data.auth.TokenStore
import com.turkcell.lyraapp.data.remote.MeApi
import com.turkcell.lyraapp.data.remote.StreamingApi
import com.turkcell.lyraapp.data.remote.dto.toDomain
import javax.inject.Inject

/**
 * Şarkı verisinin domain arayüzü.
 *
 * Uygulamanın kendi sözleşmesidir; veri kaynağından bağımsızdır. Şu an tek implementasyon
 * gerçek API'dir ([ApiSongRepository]).
 *
 * Not (§2.1): Arayüz ve tek implementasyonu, batch'in 5 dosya sınırını aşmamak için aynı
 * dosyada tutuldu.
 */
interface SongRepository {

    suspend fun getSongs(): List<Song>

    suspend fun getStreamUrl(songId: String): StreamUrl

    /** "Son Çalınanlar" — `GET /api/v1/me/recently-played` (korumalı). */
    suspend fun getRecentlyPlayed(limit: Int = 20): List<Song>

    /** "Senin İçin Müzikler" — `GET /api/v1/me/for-you` (korumalı). */
    suspend fun getForYou(limit: Int = 20): List<Song>

    /** "Öneriler" — `GET /api/v1/me/recommendations` (korumalı). */
    suspend fun getRecommendations(limit: Int = 20): List<Song>
}

/**
 * [SongRepository]'nin Streaming API implementasyonu.
 *
 * Katalog (`GET /api/v1/songs`) [StreamingApi] üzerinden public çağrılır. Anasayfanın
 * kişiselleştirilmiş bölümleri ise korumalı `me` grubu uçlarıdır: Bearer token [TokenStore]'dan
 * okunup [MeApi]'ye `@Header` ile geçirilir (mevcut [com.turkcell.lyraapp.data.auth.ApiOtpAuthRepository]
 * deseni). DTO'lar domain [Song]'a çevrilir.
 */
class ApiSongRepository @Inject constructor(
    private val api: StreamingApi,
    private val meApi: MeApi,
    private val tokenStore: TokenStore,
) : SongRepository {

    override suspend fun getSongs(): List<Song> =
        api.getSongs().data.map { it.toDomain() }

    override suspend fun getStreamUrl(songId: String): StreamUrl =
        api.getStreamUrl(songId).data.toDomain()

    override suspend fun getRecentlyPlayed(limit: Int): List<Song> =
        meApi.getRecentlyPlayed(bearer(), limit).data.map { it.toDomain() }

    override suspend fun getForYou(limit: Int): List<Song> =
        meApi.getForYou(bearer(), limit).data.map { it.toDomain() }

    override suspend fun getRecommendations(limit: Int): List<Song> =
        meApi.getRecommendations(bearer(), limit).data.map { it.toDomain() }

    /** Korumalı `me` grubu çağrıları için `Authorization` header değeri; oturum yoksa hata fırlatır. */
    private fun bearer(): String {
        val token = tokenStore.accessToken
            ?: throw IllegalStateException("Oturum yok (erişim token'ı bulunamadı).")
        return "Bearer $token"
    }
}
