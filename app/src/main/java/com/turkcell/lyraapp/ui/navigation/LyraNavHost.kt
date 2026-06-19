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
import com.turkcell.lyraapp.ui.player.PlayerScreen
import com.turkcell.lyraapp.ui.playlistdetail.PlaylistDetailScreen
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
                onNavigateToRegister = { navController.navigate(LyraDestinations.REGISTER) },
                onNavigateToHome = {
                    navController.navigate(LyraDestinations.HOME) {
                        // Giriş sonrası login geri yığından temizlenir (geri tuşuyla dönülmez).
                        popUpTo(LyraDestinations.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(LyraDestinations.REGISTER) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.popBackStack(LyraDestinations.LOGIN, inclusive = false)
                },
            )
        }
        // Ana kabuk: kendi bottom navigation + nested NavHost'unu barındırır.
        composable(LyraDestinations.HOME) {
            HomeScreen(
                onSongClick = { songId, title, artist ->
                    navController.navigate(LyraDestinations.playerRoute(songId, title, artist))
                },
                onPlaylistClick = { playlistId ->
                    navController.navigate(LyraDestinations.playlistDetailRoute(playlistId))
                },
                onCreatePlaylist = { navController.navigate(LyraDestinations.CREATE_PLAYLIST) },
            )
        }
        // Yeni çalma listesi oluşturma (tam ekran; BNB'nin üstünü kaplar). Kaydet işlevsiz (uç nokta yok).
        composable(LyraDestinations.CREATE_PLAYLIST) {
            CreatePlaylistScreen(onNavigateBack = { navController.popBackStack() })
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
        ) {
            PlaylistDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onSongClick = { songId, title, artist ->
                    navController.navigate(LyraDestinations.playerRoute(songId, title, artist))
                },
            )
        }
    }
}
