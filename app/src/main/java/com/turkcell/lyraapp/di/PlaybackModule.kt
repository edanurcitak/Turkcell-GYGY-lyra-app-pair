package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.playback.ApiPlaybackRepository
import com.turkcell.lyraapp.data.playback.PlaybackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Free akış oynatma bağımlılıklarını sağlayan Hilt modülü (FeedModule/PlaylistModule deseni).
 *
 * [PlaybackRepository] arayüzünü `me/playback` grubu implementasyonuna ([ApiPlaybackRepository]) bağlar.
 * (Ağ bağımlılıkları [NetworkModule]'de sağlanır.)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlaybackModule {

    @Binds
    @Singleton
    abstract fun bindPlaybackRepository(impl: ApiPlaybackRepository): PlaybackRepository
}
