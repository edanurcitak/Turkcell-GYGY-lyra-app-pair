package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.playlist.ApiPlaylistRepository
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Çalma listesi bağımlılıklarını sağlayan Hilt modülü (FeedModule deseni).
 *
 * [PlaylistRepository] arayüzünü gerçek API implementasyonuna ([ApiPlaylistRepository]) bağlar.
 * (Ağ bağımlılıkları [NetworkModule]'de sağlanır.)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlaylistModule {

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: ApiPlaylistRepository): PlaylistRepository
}
