package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.download.DownloadRepository
import com.turkcell.lyraapp.data.download.MediaDownloadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * İndirme bağımlılıklarını sağlayan Hilt modülü (FeedModule/PlaylistModule deseni).
 *
 * [DownloadRepository] arayüzünü cache implementasyonuna ([MediaDownloadRepository]) bağlar.
 * (Cache [MediaCacheModule]'de sağlanır; [com.turkcell.lyraapp.data.download.DownloadStore]
 * doğrudan `@Inject` ile çözülür.)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DownloadModule {

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: MediaDownloadRepository): DownloadRepository
}
