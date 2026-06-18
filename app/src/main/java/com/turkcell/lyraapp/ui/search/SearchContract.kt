package com.turkcell.lyraapp.ui.search

/**
 * Ara ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [SearchUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [SearchIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 *
 * Not: [genres] statik bir tür listesidir (API karşılığı yok, §2.2). FeedUiState'teki
 * statik alanlarla aynı mantıkla varsayılan değerde tutulur; kartlar şimdilik bir aksiyon
 * tetiklemez (talep edilmedi, §4.6).
 */
data class SearchUiState(
    val query: String = "",
    val genres: List<SearchGenre> = defaultGenres,
)

sealed interface SearchIntent {
    data class QueryChanged(val value: String) : SearchIntent
}

/**
 * "Türlere göz at" kartının saf kimlik modeli.
 *
 * Renk tutmaz: kart gradient'leri temadan türetilir ve renkler yalnızca `@Composable`
 * içinde (MaterialTheme.colorScheme) çözülebilir; bu yüzden renk seçimi çizim katmanında yapılır.
 */
data class SearchGenre(
    val id: String,
    val name: String,
)

/** "Türlere göz at" kartlarının statik listesi (ekran görüntüsü referansı). */
val defaultGenres: List<SearchGenre> = listOf(
    SearchGenre("pop", "Pop"),
    SearchGenre("electronic", "Elektronik"),
    SearchGenre("acoustic", "Akustik"),
    SearchGenre("lofi", "Lo-fi"),
    SearchGenre("indie", "Indie"),
    SearchGenre("jazz", "Jazz"),
    SearchGenre("classical", "Klasik"),
    SearchGenre("journey", "Yolculuk"),
)
