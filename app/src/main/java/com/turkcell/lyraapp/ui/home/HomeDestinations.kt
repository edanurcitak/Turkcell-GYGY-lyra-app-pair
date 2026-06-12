package com.turkcell.lyraapp.ui.home

/**
 * Home kabuğundaki bottom navigation sekmelerinin route sabitleri.
 *
 * Bu rotalar, [HomeScreen] içindeki nested NavHost'a aittir (uygulama düzeyindeki
 * [com.turkcell.lyraapp.ui.navigation.LyraDestinations]'tan ayrıdır).
 */
object HomeDestinations {
    const val FEED = "home/feed"          // Ana sayfa
    const val SEARCH = "home/search"      // Ara
    const val LIBRARY = "home/library"    // Kütüphane
    const val FAVORITES = "home/favorites" // Favoriler
    const val PROFILE = "home/profile"    // Profil
}
