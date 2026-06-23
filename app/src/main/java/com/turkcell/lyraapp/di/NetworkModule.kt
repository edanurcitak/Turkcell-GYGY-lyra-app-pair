package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.remote.AuthApi
import com.turkcell.lyraapp.data.remote.MeApi
import com.turkcell.lyraapp.data.remote.StreamingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Ağ bağımlılıklarını sağlayan Hilt modülü.
 *
 * OkHttp + Retrofit + kotlinx.serialization zincirini kurar ve [StreamingApi]'yi üretir.
 * Temel adres prod sunucusudur (bkz. `docs/api/openapi.json` → `servers`).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://streaming-api.halitkalayci.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // Sözleşmedeki yeni alanlar istemciyi kırmasın.
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json, client: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideStreamingApi(retrofit: Retrofit): StreamingApi =
        retrofit.create(StreamingApi::class.java)

    /**
     * Parolasız (OTP) kimlik doğrulama uçları. Tümü public olduğundan [StreamingApi] ile aynı
     * Retrofit istemcisini paylaşır (bkz. [com.turkcell.lyraapp.data.remote.AuthApi]).
     */
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    /**
     * Korumalı `me` grubu uçları. [AuthApi] ile aynı Retrofit istemcisini paylaşır; Bearer
     * token'ı çağrı başına `@Header` ile geçirilir (bkz. [com.turkcell.lyraapp.data.remote.MeApi]).
     */
    @Provides
    @Singleton
    fun provideMeApi(retrofit: Retrofit): MeApi =
        retrofit.create(MeApi::class.java)
}
