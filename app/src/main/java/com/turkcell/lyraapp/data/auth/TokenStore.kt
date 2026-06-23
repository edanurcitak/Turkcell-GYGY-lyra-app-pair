package com.turkcell.lyraapp.data.auth

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Oturum token'larının uygulama-içi (bellek) deposu.
 *
 * `auth/otp/verify` ve `auth/refresh` başarılı olduğunda token çiftini tutar; `me` grubu uçlarına
 * Bearer eklenmesi ve `auth/refresh`/`auth/logout` çağrıları bu depodan okur.
 *
 * Not (kullanıcı kararı): Token'lar **yalnızca bellekte** tutulur — uygulama kapanınca oturum
 * düşer. Kalıcı oturum (DataStore ile diske yazma) ileride bu sınıfın arkasına eklenebilir;
 * arayüzü (`save`/`clear` + alanlar) aynı kaldığından çağıranlar etkilenmez.
 *
 * Erişim farklı coroutine/thread'lerden olabileceği için alanlar `@Volatile`'dır.
 */
@Singleton
class TokenStore @Inject constructor() {

    @Volatile
    var accessToken: String? = null
        private set

    @Volatile
    var refreshToken: String? = null
        private set

    /** Oturum açık mı (erişim token'ı var mı). */
    val isLoggedIn: Boolean
        get() = accessToken != null

    /** Doğrulama/yenileme sonrası token çiftini kaydeder. */
    fun save(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    /** Oturumu temizler (logout). */
    fun clear() {
        accessToken = null
        refreshToken = null
    }
}
