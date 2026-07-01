package com.turkcell.lyraapp.data.favorites

import com.turkcell.lyraapp.data.auth.UserStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Beğenilen ("favori") şarkı id'lerini tutan bellek-içi reaktif depo ([DownloadStore] deseni:
 * `@Singleton @Inject`, sıcak [StateFlow]).
 *
 * **Kaynak API'dir** (§2.2 — istemci uydurmaz): favoriler için ayrı bir uç olmadığından, "Beğenilen
 * Şarkılar" adlı bir kullanıcı çalma listesiyle temsil edilir. Bu depo yalnızca o listenin
 * içeriğini (şarkı id'leri) ve çözümlenmiş liste id'sini önbelleğe alır; böylece Player'daki kalp
 * ve Kütüphane, her beğenide API'yi yeniden çekmeden anında güncellenir (bkz. [FavoritesRepository]).
 *
 * [likedIds] sıcak bir akış olduğundan bir şarkı beğenilince/çıkarılınca ona abone tüm ekranlar
 * otomatik tepki verir. Kullanıcı değişince ([UserStore]) önbellek sıfırlanır — favoriler kullanıcıya
 * özeldir; repository yeni kullanıcı için yeniden çözer/çeker.
 */
@Singleton
class FavoritesStore @Inject constructor(
    userStore: UserStore,
) {

    // Singleton uygulama ömrü boyunca yaşar; kullanıcı akışını dinlemek için iptal edilmez.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _likedIds = MutableStateFlow<Set<String>>(emptySet())

    /** Sıcak akış: bir şarkı beğenilince/çıkarılınca abone ekranlar (Player, Kütüphane) güncellenir. */
    val likedIds: StateFlow<Set<String>> = _likedIds.asStateFlow()

    /** Çözümlenmiş "Beğenilen Şarkılar" kullanıcı çalma listesinin id'si; henüz oluşmadıysa `null`. */
    @Volatile
    var likedPlaylistId: String? = null

    init {
        // Kullanıcı değişince (login/logout) önbelleği temizle; repository yeniden çözer/çeker.
        userStore.userFlow
            .onEach { reset() }
            .launchIn(scope)
    }

    /** Şarkı şu an beğenilenler kümesinde mi (önbellekten anlık; ağ dokunmaz). */
    fun isLiked(songId: String): Boolean = _likedIds.value.contains(songId)

    /** Beğenilen id kümesini API'den gelen içerikle tamamen değiştirir (refresh/detay sonrası). */
    fun setAll(ids: Collection<String>) {
        _likedIds.value = ids.toSet()
    }

    /** Başarılı "beğen" sonrası id'yi ekler (iyimser güncelleme). */
    fun add(songId: String) {
        _likedIds.update { it + songId }
    }

    /** Başarılı "beğeniyi kaldır" sonrası id'yi çıkarır (iyimser güncelleme). */
    fun remove(songId: String) {
        _likedIds.update { it - songId }
    }

    /** Kullanıcı değişiminde önbelleği (id kümesi + çözümlenmiş liste id'si) sıfırlar. */
    private fun reset() {
        _likedIds.value = emptySet()
        likedPlaylistId = null
    }
}
