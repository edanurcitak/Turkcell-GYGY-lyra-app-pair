package com.turkcell.lyraapp.data.feed

/**
 * Ana sayfa verisinin domain arayüzü.
 *
 * Uygulamanın kendi sözleşmesidir; veri kaynağından (mock / gerçek API) bağımsızdır.
 * Gerçek backend bağlandığında yalnızca implementasyon değişir.
 */
interface FeedRepository {

    suspend fun getHomeFeed(): HomeFeed
}
