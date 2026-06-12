package com.turkcell.lyraapp.data.auth

/**
 * Kimlik (auth) işlemlerinin domain arayüzü.
 *
 * Bu arayüz uygulamanın kendi sözleşmesidir; backend REST API'sine bağımlı değildir.
 * Gerçek ağ entegrasyonu yapıldığında yalnızca implementasyon değişir, bu arayüz ve
 * ona bağımlı ViewModel'ler aynı kalır.
 */
interface AuthRepository {

    suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit>
}
