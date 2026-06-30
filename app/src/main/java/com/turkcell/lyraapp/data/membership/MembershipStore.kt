package com.turkcell.lyraapp.data.membership

import com.turkcell.lyraapp.data.auth.Membership
import com.turkcell.lyraapp.data.auth.User
import com.turkcell.lyraapp.data.auth.isPremium
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Oturum açan kullanıcının üyelik tier'ını (free/premium) tutan bellek-içi depo
 * ([com.turkcell.lyraapp.data.auth.TokenStore] deseni: `@Singleton @Inject`, arayüzsüz).
 *
 * **Tier kaynağı API'dir** (§2.2 — istemci hesaplamaz): login (`/auth/otp/verify`) yanıtındaki
 * `user.membership` alanından okunup buraya yazılır (bkz.
 * [com.turkcell.lyraapp.data.auth.ApiOtpAuthRepository]). Burada yalnızca **aynalanır**; yetki
 * zaten sunucuda zorlanır (ör. `stream-url` free'ye 403). İstemci bunu UI'ı sürmek (indir butonu
 * açık/kilitli) ve doğru akışı seçmek için kullanır.
 *
 * Token'lar gibi bellek-içidir; her uygulama açılışında login tekrar set eder.
 */
@Singleton
class MembershipStore @Inject constructor() {

    private val _isPremium = MutableStateFlow(false)

    /** Sıcak akış: UI (örn. Player indir butonu) tier değişimine tepki verebilir. */
    val isPremiumFlow: StateFlow<Boolean> = _isPremium.asStateFlow()

    /** Anlık premium kontrolü (indirme öncesi gibi senkron kararlar için). */
    val isPremium: Boolean
        get() = _isPremium.value

    /** Login sonrası kullanıcıdan tier'ı set eder (aktif üyelik → premium). */
    fun setFromUser(user: User?) {
        _isPremium.value = user?.isPremium == true
    }

    /**
     * Checkout sonrası satın alınan üyelikten tier'ı set eder (onaylı satın alma → premium).
     *
     * Tier yine API yanıtından gelen [Membership]'ten türetilir ([Membership.isActive]); istemci
     * hesaplamaz, yalnızca aynalar (§2.2). Banner ve premium özellikler buna reaktif olarak açılır.
     */
    fun setActive(membership: Membership) {
        _isPremium.value = membership.isActive
    }

    /** Logout: tier'ı sıfırla (free). */
    fun clear() {
        _isPremium.value = false
    }
}
