package com.turkcell.lyraapp.data.membership

import com.turkcell.lyraapp.data.remote.StreamingApi
import com.turkcell.lyraapp.data.remote.dto.toDomain
import javax.inject.Inject

/**
 * Satın alınabilir premium planın domain modeli.
 *
 * Streaming API'nin `MembershipPlan` şemasının (bkz. `docs/api/openapi.json`) uygulama-içi
 * karşılığıdır. Yalnızca UI'ın ihtiyaç duyduğu alanlar tutulur. Fiyat, hassasiyet için kuruş
 * cinsinden ([priceKurus]) tutulur; gösterim biçimi (₺139,00) UI katmanında türetilir.
 *
 * Not: Kullanıcının aktif üyelik durumunu tutan [com.turkcell.lyraapp.data.auth.Membership]'ten
 * ayrıdır; bu, satın alınabilir plan kataloğudur.
 */
data class MembershipPlan(
    val id: String,
    /** "one-time" | "recurring". */
    val type: String,
    val name: String,
    val description: String,
    val priceKurus: Int,
    val currency: String,
    val durationDays: Int,
    val autoRenew: Boolean,
)

/**
 * Premium plan kataloğunun domain arayüzü.
 *
 * Uygulamanın kendi sözleşmesidir; veri kaynağından bağımsızdır. Şu an tek implementasyon
 * gerçek API'dir ([ApiMembershipRepository]).
 *
 * Not (§2.1): Arayüz ve tek implementasyonu, batch'in 5 dosya sınırını aşmamak için aynı
 * dosyada tutuldu ([com.turkcell.lyraapp.data.feed.SongRepository] deseni).
 */
interface MembershipRepository {

    /** Satın alınabilir premium planlar — `GET /api/v1/memberships/plans` (public). */
    suspend fun getPlans(): List<MembershipPlan>
}

/**
 * [MembershipRepository]'nin Streaming API implementasyonu.
 *
 * Plan kataloğu public olduğundan token gerektirmez; diğer public uçlar gibi [StreamingApi]
 * üzerinden çağrılır. DTO'lar domain [MembershipPlan]'a çevrilir ([ApiSongRepository] deseni).
 */
class ApiMembershipRepository @Inject constructor(
    private val api: StreamingApi,
) : MembershipRepository {

    override suspend fun getPlans(): List<MembershipPlan> =
        api.getMembershipPlans().data.map { it.toDomain() }
}
