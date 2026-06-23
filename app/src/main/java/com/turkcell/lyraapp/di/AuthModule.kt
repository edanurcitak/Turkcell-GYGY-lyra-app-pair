package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.auth.ApiOtpAuthRepository
import com.turkcell.lyraapp.data.auth.OtpAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Auth bağımlılıklarını sağlayan Hilt modülü.
 *
 * Parolasız (OTP) kimlik doğrulama arayüzünü ([OtpAuthRepository]) gerçek ağ implementasyonuna
 * ([ApiOtpAuthRepository]) bağlar.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindOtpAuthRepository(impl: ApiOtpAuthRepository): OtpAuthRepository
}
