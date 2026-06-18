package com.turkcell.lyraapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turkcell.lyraapp.ui.home.HomeScreen
import com.turkcell.lyraapp.ui.login.LoginScreen
import com.turkcell.lyraapp.ui.player.PlayerScreen
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
                onSongClick = { songId ->
                    navController.navigate(LyraDestinations.playerRoute(songId))
                },
            )
        }
        // Şarkı oynatma (tam ekran); songId argümanını taşır.
        composable(
            route = LyraDestinations.PLAYER_ROUTE,
            arguments = listOf(
                navArgument(LyraDestinations.PLAYER_ARG_SONG_ID) { type = NavType.StringType },
            ),
        ) {
            PlayerScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
