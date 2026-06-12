package com.turkcell.lyraapp.data.auth

import javax.inject.Inject

/**
 * [AuthRepository]'nin geçici (placeholder) implementasyonu.
 *
 * Ağa gitmez; DI zincirinin uçtan uca çalıştığını göstermek için her zaman başarı döner.
 * Backend REST API sözleşmesi netleştiğinde bu sınıf gerçek bir ağ implementasyonuyla
 * değiştirilecektir (bkz. [com.turkcell.lyraapp.di.AuthModule]).
 *
 * `@Inject constructor`, Hilt'in bu implementasyonu kendi başına üretebilmesini sağlar.
 */
class FakeAuthRepository @Inject constructor() : AuthRepository {

    override suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit> {
        // TODO: Gerçek API entegrasyonu (başka takımın REST servisi) bağlanacak.
        return Result.success(Unit)
    }
}
