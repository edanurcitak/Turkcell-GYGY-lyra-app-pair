package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.feed.FeedRepository
import com.turkcell.lyraapp.data.feed.MockFeedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Feed bağımlılıklarını sağlayan Hilt modülü.
 *
 * [FeedRepository] arayüzünü mevcut implementasyona ([MockFeedRepository]) bağlar.
 * Gerçek implementasyona geçişte yalnızca buradaki `@Binds` hedefi değişir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeedModule {

    @Binds
    @Singleton
    abstract fun bindFeedRepository(impl: MockFeedRepository): FeedRepository
}
