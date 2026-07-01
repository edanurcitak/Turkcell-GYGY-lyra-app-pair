package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.favorites.ApiFavoritesRepository
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Favori bağımlılıklarını sağlayan Hilt modülü (FeedModule deseni).
 *
 * [FavoritesRepository] arayüzünü gerçek API implementasyonuna ([ApiFavoritesRepository]) bağlar.
 * Reaktif önbellek [com.turkcell.lyraapp.data.favorites.FavoritesStore] `@Singleton @Inject`
 * olduğundan ayrıca bağlanmasına gerek yoktur (ağ bağımlılıkları [NetworkModule]'de sağlanır).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesModule {

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: ApiFavoritesRepository): FavoritesRepository
}
