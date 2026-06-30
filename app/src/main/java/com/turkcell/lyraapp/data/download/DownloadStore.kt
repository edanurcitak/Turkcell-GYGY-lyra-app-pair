package com.turkcell.lyraapp.data.download

import android.content.Context
import com.turkcell.lyraapp.data.auth.UserStore
import com.turkcell.lyraapp.data.feed.Song
import com.turkcell.lyraapp.data.membership.MembershipStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
 * **İki kapı (görünür indirme = kullanıcıya ait ∧ premium):**
 * - **Kullanıcıya göre bölümleme:** meta veri global değil, oturum açan kullanıcının kimliğine göre
 *   ayrı anahtarda (`downloaded_songs_<userId>`) saklanır. Böylece bir profilde indirilen şarkı başka
 *   profilde "indirilmiş" görünmez. Aktif kova [UserStore] değiştikçe ([userFlow]) yeniden okunur.
 * - **Tier kapısı:** UI'ın okuduğu [downloads] akışı [MembershipStore] ile birleştirilir; premium
 *   değilken **boş** yayılır ve [isDownloaded] daima `false` döner. Baytlar/meta **silinmez**, yalnızca
 *   gizlenir — tier kaynağı API'dir (§2.2), tekrar premium olununca aynı hesapta geri görünür.
 *
 * [downloads] sıcak bir [StateFlow] olduğundan Player (indir butonunun durumu) ve Kütüphane
 * (indirilenler listesi) indirme tamamlandığında veya tier/kullanıcı değiştiğinde otomatik güncellenir.
 *
 * Not (§2.2): Domain [Song]'u doğrudan kalıcılığa bağlamamak için ayrı bir [StoredSong] kaydı
 * serileştirilir; alanlar birebir eşlenir.
 */
@Singleton
class DownloadStore @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json,
    private val userStore: UserStore,
    private val membershipStore: MembershipStore,
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Singleton uygulama ömrü boyunca yaşar; kullanıcı/tier akışlarını dinlemek için iptal edilmez.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Oturum açan kullanıcının kimliği (kova anahtarı); oturum yoksa `null`. */
    private val activeUserId: String? get() = userStore.userFlow.value?.id

    // Mevcut kullanıcının HAM (tier'sız) indirme kovası; kullanıcı değişince yeniden doldurulur.
    private val _userDownloads = MutableStateFlow(readFromPrefs(activeUserId))

    /**
     * UI'ın okuduğu KAPILI görünüm: yalnızca premium hesapta ve yalnızca o hesaba ait indirmeler.
     * Premium değilken boş yayar (gizleme; silme yok). [SharingStarted.Eagerly] ile her zaman güncel
     * tutulur; çünkü [PlaylistRepository] gibi okuyucular sürekli abone olmadan `value`'yu okur.
     */
    val downloads: StateFlow<List<Song>> = combine(
        _userDownloads,
        membershipStore.isPremiumFlow,
    ) { songs, premium ->
        if (premium) songs else emptyList()
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = if (membershipStore.isPremium) _userDownloads.value else emptyList(),
    )

    init {
        // Kullanıcı değişince (login/logout) aktif kovayı o kullanıcının kayıtlarıyla yeniden doldur.
        userStore.userFlow
            .onEach { user -> _userDownloads.value = readFromPrefs(user?.id) }
            .launchIn(scope)
    }

    /** Şarkı, mevcut premium kullanıcı için indirilmiş mi (kapılı: free'de daima `false`). */
    fun isDownloaded(songId: String): Boolean =
        membershipStore.isPremium && _userDownloads.value.any { it.id == songId }

    /**
     * İndirme tamamlandığında çağrılır; mevcut kullanıcının kovasına yazar (premium akışından gelir,
     * bkz. [MediaDownloadRepository]). Oturum yoksa yok sayar.
     */
    fun add(song: Song) {
        val userId = activeUserId ?: return
        _userDownloads.update { current ->
            if (current.any { it.id == song.id }) current else current + song
        }
        writeToPrefs(userId, _userDownloads.value)
    }

    /** İndirmeyi mevcut kullanıcının kovasından kaldırır (cache baytları ayrıca temizlenebilir; şimdilik meta veri yeter). */
    fun remove(songId: String) {
        val userId = activeUserId ?: return
        _userDownloads.update { current -> current.filterNot { it.id == songId } }
        writeToPrefs(userId, _userDownloads.value)
    }

    private fun readFromPrefs(userId: String?): List<Song> {
        if (userId == null) return emptyList()
        val raw = prefs.getString(keyFor(userId), null) ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<StoredSong>>(raw).map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    private fun writeToPrefs(userId: String, songs: List<Song>) {
        val raw = json.encodeToString(songs.map { StoredSong.fromDomain(it) })
        prefs.edit().putString(keyFor(userId), raw).apply()
    }

    private fun keyFor(userId: String): String = "$KEY_DOWNLOADS_PREFIX$userId"

    private companion object {
        const val PREFS_NAME = "lyra_downloads"
        const val KEY_DOWNLOADS_PREFIX = "downloaded_songs_"
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
