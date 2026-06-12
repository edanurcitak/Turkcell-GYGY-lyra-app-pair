package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.auth.FakeAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Auth bağımlılıklarını sağlayan Hilt modülü.
 *
 * [AuthRepository] arayüzünü mevcut implementasyona ([FakeAuthRepository]) bağlar.
 * Gerçek implementasyona geçişte yalnızca buradaki `@Binds` hedefi değişir.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository
}
