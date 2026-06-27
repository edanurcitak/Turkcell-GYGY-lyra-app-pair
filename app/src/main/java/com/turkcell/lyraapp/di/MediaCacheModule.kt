package com.turkcell.lyraapp.di

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/**
 * Çevrimdışı oynatma için medya cache'ini sağlayan Hilt modülü.
 *
 * Tek bir [SimpleCache] örneği üretir; hem indirme ([com.turkcell.lyraapp.data.download.DownloadRepository]
 * şarkıyı tamamen cache'e yazar) hem de oynatma ([com.turkcell.lyraapp.ui.player.PlaybackService]
 * `CacheDataSource` üzerinden okur) aynı cache'i ve aynı cache key'ini (`songId`) paylaşır.
 * Böylece indirilmiş şarkı, internet veya yeni bir stream-url isteği olmadan cihaz belleğinden çalar.
 *
 * Notlar:
 * - SimpleCache bir dizin için süreç başına **tek** örnek olmalıdır (aksi halde kilit hatası verir);
 *   `@Singleton` bunu garanti eder.
 * - Konum kalıcı iç depolamadır (`filesDir`) — OS tarafından temizlenmez; indirilenler uygulama
 *   silinene/verisi temizlenene dek durur.
 * - [NoOpCacheEvictor]: indirilen şarkılar otomatik tahliye edilmez (çevrimdışı erişim garantisi).
 *   Buna karşılık oynatma yolu salt-okunur açıldığından (bkz. PlaybackService) cache yalnızca
 *   bilinçli indirmelerle dolar, sınırsız şişmez.
 */
@Module
@InstallIn(SingletonComponent::class)
object MediaCacheModule {

    @Provides
    @Singleton
    fun provideMediaCache(@ApplicationContext context: Context): SimpleCache {
        val cacheDir = File(context.filesDir, MEDIA_CACHE_DIR)
        return SimpleCache(
            cacheDir,
            NoOpCacheEvictor(),
            StandaloneDatabaseProvider(context),
        )
    }

    private const val MEDIA_CACHE_DIR = "lyra-media-cache"
}
