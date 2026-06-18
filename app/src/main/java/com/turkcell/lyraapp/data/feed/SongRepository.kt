package com.turkcell.lyraapp.data.feed

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
}

/**
 * [SongRepository]'nin Streaming API implementasyonu.
 *
 * `GET /api/v1/songs` ilk sayfasını çeker ve DTO'ları domain [Song]'a çevirir. Sayfalama
 * (`nextCursor`) ve arama (`q`) sonraki adımların kapsamındadır.
 */
class ApiSongRepository @Inject constructor(
    private val api: StreamingApi,
) : SongRepository {

    override suspend fun getSongs(): List<Song> =
        api.getSongs().data.map { it.toDomain() }

    override suspend fun getStreamUrl(songId: String): StreamUrl =
        api.getStreamUrl(songId).data.toDomain()
}
