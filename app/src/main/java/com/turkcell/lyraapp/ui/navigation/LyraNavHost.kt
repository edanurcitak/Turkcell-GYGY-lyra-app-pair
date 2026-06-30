package com.turkcell.lyraapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turkcell.lyraapp.ui.createplaylist.CreatePlaylistScreen
import com.turkcell.lyraapp.ui.home.HomeScreen
import com.turkcell.lyraapp.ui.login.LoginScreen
import com.turkcell.lyraapp.ui.otp.OtpScreen
import com.turkcell.lyraapp.ui.payment.PaymentScreen
import com.turkcell.lyraapp.ui.payment.PaymentSuccessScreen
import com.turkcell.lyraapp.ui.player.PlayerScreen
import com.turkcell.lyraapp.ui.playlistdetail.PlaylistDetailScreen
import com.turkcell.lyraapp.ui.premium.PremiumPlansScreen
import com.turkcell.lyraapp.ui.register.RegisterScreen

/**
 * Uygulamanın iskelet navigasyon grafiği (decisions.md: Compose Navigation).
 *
 * Ekranlar navigasyonu yalnızca callback'ler üzerinden alır; `NavController` ekran
 * composable'larına veya ViewModel'lere sızmaz (single-responsibility / test edilebilirlik).
 */
@Composable
fun LyraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = LyraDestinations.LOGIN,
        modifier = modifier,
    ) {
        composable(LyraDestinations.LOGIN) {
            LoginScreen(
                // Telefon için OTP istendikten sonra doğrulama ekranına geç.
                onNavigateToOtp = { phone ->
                    navController.navigate(LyraDestinations.otpRoute(phone))
                },
            )
        }
        // OTP doğrulama ekranı; phone path argümanını taşır. Doğrulama sonrası:
        // firstTime → profil tamamlama (register), kayıtlı → ana ekran.
        composable(
            route = LyraDestinations.OTP_ROUTE,
            arguments = listOf(
                navArgument(LyraDestinations.OTP_ARG_PHONE) { type = NavType.StringType },
            ),
        ) {
            OtpScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCompleteInfo = { navController.navigate(LyraDestinations.REGISTER) },
                onNavigateToHome = {
                    navController.navigate(LyraDestinations.HOME) {
                        // Giriş sonrası login/otp geri yığından temizlenir (geri tuşuyla dönülmez).
                        popUpTo(LyraDestinations.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        // Profil tamamlama ("Bilgilerini tamamla"); başarıda ana ekrana geçer.
        composable(LyraDestinations.REGISTER) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(LyraDestinations.HOME) {
                        popUpTo(LyraDestinations.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        // Ana kabuk: kendi bottom navigation + nested NavHost'unu barındırır.
        composable(LyraDestinations.HOME) {
            HomeScreen(
                onSongClick = { songId, title, artist ->
                    // Feed'den açılır: önceki/sonraki katalog (getSongs) içinde gezer.
                    navController.navigate(
                        LyraDestinations.playerRoute(songId, title, artist, LyraDestinations.QUEUE_FEED),
                    )
                },
                onPlaylistClick = { playlistId ->
                    navController.navigate(LyraDestinations.playlistDetailRoute(playlistId))
                },
                onCreatePlaylist = { navController.navigate(LyraDestinations.CREATE_PLAYLIST) },
                onNavigateToPremium = { navController.navigate(LyraDestinations.PREMIUM_PLANS) },
            )
        }
        // Yeni çalma listesi oluşturma (tam ekran; BNB'nin üstünü kaplar). Kaydet işlevsiz (uç nokta yok).
        composable(LyraDestinations.CREATE_PLAYLIST) {
            CreatePlaylistScreen(onNavigateBack = { navController.popBackStack() })
        }
        // Premium plan seçimi (tam ekran; Profil'deki free banner'dan açılır). "Devam et" → ödeme.
        composable(LyraDestinations.PREMIUM_PLANS) {
            PremiumPlansScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinue = { planId ->
                    navController.navigate(LyraDestinations.paymentRoute(planId))
                },
            )
        }
        // Ödeme (tam ekran); seçilen planId path argümanını taşır. Onayda (premium aktif) profile döner.
        composable(
            route = LyraDestinations.PAYMENT_ROUTE,
            arguments = listOf(
                navArgument(LyraDestinations.PAYMENT_ARG_PLAN_ID) { type = NavType.StringType },
            ),
        ) {
            PaymentScreen(
                onNavigateBack = { navController.popBackStack() },
                // Başarıda ödeme başarılı ekranına geç; premium/ödeme ekranlarını yığından temizle ki
                // başarı ekranı doğrudan ana kabuğun (HOME) üstünde kalsın (geri tuşu ödemeye dönmez).
                onPaymentSuccess = {
                    navController.navigate(LyraDestinations.PAYMENT_SUCCESS) {
                        popUpTo(LyraDestinations.PREMIUM_PLANS) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        // Ödeme başarılı (tam ekran). "Dinlemeye başla" → profil (ana kabuk; profil sekmesi korunur).
        // Banner MembershipStore'dan reaktif olarak premium'a güncellenmiştir.
        composable(LyraDestinations.PAYMENT_SUCCESS) {
            PaymentSuccessScreen(
                onStartListening = {
                    navController.popBackStack(LyraDestinations.HOME, inclusive = false)
                },
            )
        }
        // Şarkı oynatma (tam ekran); songId path + title/artist query argümanlarını taşır.
        composable(
            route = LyraDestinations.PLAYER_ROUTE,
            arguments = listOf(
                navArgument(LyraDestinations.PLAYER_ARG_SONG_ID) { type = NavType.StringType },
                navArgument(LyraDestinations.PLAYER_ARG_TITLE) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(LyraDestinations.PLAYER_ARG_ARTIST) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(LyraDestinations.PLAYER_ARG_QUEUE) {
                    type = NavType.StringType
                    defaultValue = LyraDestinations.QUEUE_FEED
                },
            ),
        ) {
            PlayerScreen(onNavigateBack = { navController.popBackStack() })
        }
        // Çalma listesi detayı (tam ekran); playlistId argümanını taşır. Şarkı tıklaması player'a gider.
        composable(
            route = LyraDestinations.PLAYLIST_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(LyraDestinations.PLAYLIST_DETAIL_ARG_ID) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments
                ?.getString(LyraDestinations.PLAYLIST_DETAIL_ARG_ID)
                .orEmpty()
            PlaylistDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onSongClick = { songId, title, artist ->
                    // Çalma listesinden açılır: önceki/sonraki bu listenin şarkıları içinde gezer.
                    navController.navigate(
                        LyraDestinations.playerRoute(
                            songId, title, artist, LyraDestinations.queuePlaylist(playlistId),
                        ),
                    )
                },
            )
        }
    }
}
