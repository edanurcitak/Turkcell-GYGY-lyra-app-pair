package com.turkcell.lyraapp.data.download

import android.content.Context
import com.turkcell.lyraapp.data.feed.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * İndirilen şarkıların meta verisini tutan kalıcı depo ([com.turkcell.lyraapp.data.auth.TokenStore]
 * deseni: `@Singleton @Inject`).
 *
 * Cache (ham ses baytları) [androidx.media3.datasource.cache.SimpleCache]'te `songId` anahtarıyla
 * tutulur; cache yalnızca bayt saklar, şarkı başlığı/sanatçısı tutmaz. "İndirilen Şarkılar"
 * listesini çizebilmek için meta veriyi burada SharedPreferences + JSON ile saklarız.
 *
 * [downloads] sıcak bir [StateFlow] olduğundan Player (indir butonunun durumu) ve Kütüphane
 * (indirilenler listesi) indirme tamamlandığında otomatik güncellenir.
 *
 * Not (§2.2): Domain [Song]'u doğrudan kalıcılığa bağlamamak için ayrı bir [StoredSong] kaydı
 * serileştirilir; alanlar birebir eşlenir.
 */
@Singleton
class DownloadStore @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json,
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _downloads = MutableStateFlow(readFromPrefs())
    val downloads: StateFlow<List<Song>> = _downloads.asStateFlow()

    /** Şarkı indirilmiş mi (meta veri kaydı var mı). */
    fun isDownloaded(songId: String): Boolean =
        _downloads.value.any { it.id == songId }

    /** İndirme tamamlandığında çağrılır; aynı şarkı varsa yinelemez. */
    fun add(song: Song) {
        _downloads.update { current ->
            if (current.any { it.id == song.id }) current else current + song
        }
        writeToPrefs(_downloads.value)
    }

    /** İndirmeyi listeden kaldırır (cache baytları ayrıca temizlenebilir; şimdilik meta veri yeter). */
    fun remove(songId: String) {
        _downloads.update { current -> current.filterNot { it.id == songId } }
        writeToPrefs(_downloads.value)
    }

    private fun readFromPrefs(): List<Song> {
        val raw = prefs.getString(KEY_DOWNLOADS, null) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<StoredSong>>(raw).map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    private fun writeToPrefs(songs: List<Song>) {
        val raw = json.encodeToString(songs.map { StoredSong.fromDomain(it) })
        prefs.edit().putString(KEY_DOWNLOADS, raw).apply()
    }

    private companion object {
        const val PREFS_NAME = "lyra_downloads"
        const val KEY_DOWNLOADS = "downloaded_songs"
    }
}

/** [Song]'un kalıcılık (JSON) karşılığı; domain modelini serileştirmeden ayrı tutar. */
@Serializable
private data class StoredSong(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Long,
) {
    fun toDomain(): Song = Song(id, title, artist, album, durationMs)

    companion object {
        fun fromDomain(song: Song): StoredSong =
            StoredSong(song.id, song.title, song.artist, song.album, song.durationMs)
    }
}
