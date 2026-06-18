package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.feed.ApiSongRepository
import com.turkcell.lyraapp.data.feed.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Feed bağımlılıklarını sağlayan Hilt modülü.
 *
 * [SongRepository] arayüzünü gerçek API implementasyonuna ([ApiSongRepository]) bağlar.
 * (Ağ bağımlılıkları [NetworkModule]'de sağlanır.)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeedModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(impl: ApiSongRepository): SongRepository
}
