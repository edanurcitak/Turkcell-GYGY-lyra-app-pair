package com.turkcell.lyraapp.data.membership

import com.turkcell.lyraapp.data.auth.Membership
import com.turkcell.lyraapp.data.auth.TokenStore
import com.turkcell.lyraapp.data.remote.StreamingApi
import com.turkcell.lyraapp.data.remote.dto.CardDto
import com.turkcell.lyraapp.data.remote.dto.CheckoutBody
import com.turkcell.lyraapp.data.remote.dto.toDomain
import retrofit2.HttpException
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

    /**
     * Premium üyelik satın alır (mock kart ödemesi) — `POST /api/v1/memberships/checkout` (korumalı).
     *
     * @param plan satın alınacak planın türü ("one-time" | "recurring", [MembershipPlan.type])
     * @param card mock kart bilgileri (sonuç [CardInput.number]'a göre belirlenir)
     * @return onayda aktif [Membership]; başarısızlık [Result] içinde döner — kart reddinde
     * ([PaymentDeclinedException]) çağıran (ViewModel) kullanıcıya özel mesaj gösterebilir.
     */
    suspend fun checkout(plan: String, card: CardInput): Result<Membership>
}

/**
 * Checkout için mock kart girdisi (domain modeli; DTO'dan bağımsız).
 *
 * [number] gruplanmış ("4242 4242 4242 4242") veya düz olabilir; sonuç buna göre belirlenir.
 * [expYear] dört haneli yıldır (ör. 2030).
 */
data class CardInput(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String,
)

/**
 * Checkout 402 — kart reddedildi (test kartı `4000 0000 0000 0002` ya da geçersiz kart).
 *
 * Retrofit'in [HttpException]'ı data katmanında bu domain hatasına çevrilir; böylece ViewModel
 * "reddedildi"yi diğer hatalardan ayırt edip özel mesaj gösterirken ağ detayına bağımlı olmaz.
 */
class PaymentDeclinedException : Exception("Ödeme reddedildi.")

/**
 * [MembershipRepository]'nin Streaming API implementasyonu.
 *
 * Plan kataloğu public olduğundan token gerektirmez; diğer public uçlar gibi [StreamingApi]
 * üzerinden çağrılır. DTO'lar domain [MembershipPlan]'a çevrilir ([ApiSongRepository] deseni).
 */
class ApiMembershipRepository @Inject constructor(
    private val api: StreamingApi,
    private val tokenStore: TokenStore,
    private val membershipStore: MembershipStore,
) : MembershipRepository {

    override suspend fun getPlans(): List<MembershipPlan> =
        api.getMembershipPlans().data.map { it.toDomain() }

    override suspend fun checkout(plan: String, card: CardInput): Result<Membership> = runCatching {
        val token = tokenStore.accessToken
            ?: throw IllegalStateException("Ödeme için oturum yok (erişim token'ı bulunamadı).")
        val data = try {
            api.checkout(
                authorization = "Bearer $token",
                body = CheckoutBody(
                    plan = plan,
                    card = CardDto(
                        number = card.number,
                        expMonth = card.expMonth,
                        expYear = card.expYear,
                        cvc = card.cvc,
                        holderName = card.holderName,
                    ),
                ),
            ).data
        } catch (e: HttpException) {
            // 402 → kart reddi: domain hatasına çevir (ağ detayı sınırda kalır).
            if (e.code() == 402) throw PaymentDeclinedException() else throw e
        }
        val membership = data.membership?.toDomain()
            ?: throw IllegalStateException("Ödeme yanıtında üyelik bilgisi yok.")
        // Tier'ı API yanıtından aynalama (onaylı satın alma → premium); §2.2 — istemci hesaplamaz.
        membershipStore.setActive(membership)
        membership
    }
}
