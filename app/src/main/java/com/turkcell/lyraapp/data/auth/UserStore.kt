package com.turkcell.lyraapp.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Oturum açan kullanıcının kimlik bilgisini ([User]) tutan bellek-içi depo
 * ([TokenStore]/[com.turkcell.lyraapp.data.membership.MembershipStore] deseni:
 * `@Singleton @Inject`, arayüzsüz, modülsüz).
 *
 * **Kaynak API'dir** (§2.2 — istemci uydurmaz): login (`/auth/otp/verify`) ve profil tamamlama
 * (`/me/update-informations`) yanıtlarındaki `user` buraya yazılır (bkz. [ApiOtpAuthRepository]).
 * Burada yalnızca **aynalanır**; UI (örn. Profil ekranı ad/baş harf) bu sıcak akışı dinler.
 *
 * Token'lar gibi bellek-içidir; her uygulama açılışında login tekrar set eder. [MembershipStore]
 * tier'ı (free/premium) ayrı tutar; burası kimlik (ad/soyad/telefon/doğum tarihi) içindir.
 */
@Singleton
class UserStore @Inject constructor() {

    private val _userFlow = MutableStateFlow<User?>(null)

    /** Sıcak akış: UI oturum açan kullanıcının kimlik değişimine tepki verebilir. */
    val userFlow: StateFlow<User?> = _userFlow.asStateFlow()

    /** Login/profil tamamlama sonrası kullanıcıyı set eder (API yanıtından aynalanır). */
    fun setUser(user: User?) {
        _userFlow.value = user
    }

    /** Logout: kullanıcıyı temizle. */
    fun clear() {
        _userFlow.value = null
    }
}
