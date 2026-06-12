package com.turkcell.lyraapp.data.feed

import javax.inject.Inject

/**
 * [FeedRepository]'nin statik MOCK implementasyonu.
 *
 * Ağa gitmez; ekran görüntüsündeki içeriği sabit veri olarak döner. Gerçek backend REST
 * servisi bağlandığında bu sınıf gerçek implementasyonla değiştirilecektir
 * (bkz. [com.turkcell.lyraapp.di.FeedModule]).
 *
 * Not (§2.2): Bölüm 1'deki kırpık başlıklar makul tamamlandı; Bölüm 3 (çalma listeleri)
 * başlıkları ekran görüntüsünde okunaksız olduğundan temsilî mock adlar kullanıldı.
 */
class MockFeedRepository @Inject constructor() : FeedRepository {

    override suspend fun getHomeFeed(): HomeFeed = HomeFeed(
        greeting = "İyi akşamlar",
        userInitials = "ZK",
        quickPicks = listOf(
            QuickPick("qp1", "Gece Sürüşü", ArtworkTone.PRIMARY),
            QuickPick("qp2", "Sabah Kahvaltısı", ArtworkTone.TERTIARY),
            QuickPick("qp3", "Neon Sokaklar", ArtworkTone.SECONDARY),
            QuickPick("qp4", "Odaklan", ArtworkTone.NEUTRAL),
            QuickPick("qp5", "Derin Mavi", ArtworkTone.PRIMARY),
            QuickPick("qp6", "Yaz Anıları", ArtworkTone.TERTIARY),
        ),
        recentlyPlayed = listOf(
            MediaCard("rp1", "Neon Sokaklar", "Şehir Işıkları", ArtworkTone.SECONDARY),
            MediaCard("rp2", "Derin Mavi", "Okyanus", ArtworkTone.PRIMARY),
            MediaCard("rp3", "Yıldız Tozu", "Polaris", ArtworkTone.TERTIARY),
        ),
        playlists = listOf(
            MediaCard("pl1", "Akşam Sakinliği", "Lyra Mix", ArtworkTone.NEUTRAL),
            MediaCard("pl2", "Yoğunlaşma", "Lo-Fi seçkisi", ArtworkTone.PRIMARY),
            MediaCard("pl3", "Hareketli Başlangıç", "Enerji dolu", ArtworkTone.TERTIARY),
            MediaCard("pl4", "Gece Yolculuğu", "Synthwave", ArtworkTone.SECONDARY),
        ),
    )
}
