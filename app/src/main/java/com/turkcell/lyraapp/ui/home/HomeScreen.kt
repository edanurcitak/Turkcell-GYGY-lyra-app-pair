package com.turkcell.lyraapp.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.turkcell.lyraapp.ui.feed.FeedScreen
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.library.LibraryScreen
import com.turkcell.lyraapp.ui.miniplayer.MiniPlayer
import com.turkcell.lyraapp.ui.profile.ProfileScreen
import com.turkcell.lyraapp.ui.search.SearchScreen
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/** Bottom navigation sekmesini tanımlayan görsel model. */
private data class HomeTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val homeTabs = listOf(
    HomeTab(HomeDestinations.FEED, "Ana sayfa", LyraIcons.Home),
    HomeTab(HomeDestinations.SEARCH, "Ara", LyraIcons.Search),
    HomeTab(HomeDestinations.LIBRARY, "Kütüphane", LyraIcons.Library),
    HomeTab(HomeDestinations.PROFILE, "Profil", LyraIcons.Profile),
)

/**
 * Uygulamanın ana kabuğu: alt navigasyon (BNB) + nested NavHost.
 *
 * BNB `Scaffold.bottomBar`'da, sekme içerikleri ise content'teki [NavHost]'tadır; bu
 * sayede BNB her sekmenin altında kalıcı olarak görünür. Seçili sekme, nav back stack'ten
 * türetilir; ayrı bir state/ViewModel tutulmaz.
 */
@Composable
fun HomeScreen(
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onCreatePlaylist: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Mini player BNB'nin hemen üstünde, her sekmenin altında kalıcı; aktif parça
            // yokken kendini çizmez. bottomBar slotunda olduğundan içerik onun üstünü kaplamaz.
            Column {
                MiniPlayer(
                    onExpand = onSongClick,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                HomeBottomBar(navController)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestinations.FEED,
            modifier = Modifier.padding(innerPadding),
        ) {
            // Feed/Ara/Kütüphane/Profil gerçek ekranlarıyla bağlandı.
            composable(HomeDestinations.FEED) { FeedScreen(onSongClick = onSongClick) }
            composable(HomeDestinations.SEARCH) { SearchScreen() }
            composable(HomeDestinations.LIBRARY) {
                LibraryScreen(
                    onPlaylistClick = onPlaylistClick,
                    onCreatePlaylistClick = onCreatePlaylist,
                )
            }
            composable(HomeDestinations.PROFILE) {
                ProfileScreen(
                    onNavigateToPremiumPlans = onNavigateToPremium,
                    onNavigateToLogin = onLogout,
                )
            }
        }
    }
}

@Composable
private fun HomeBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        homeTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        // Sekmeler arası geçişte tek instance + state korunur, geri yığını şişmez.
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Preview(name = "Home • Dark", showBackground = true)
@Composable
private fun HomeScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        HomeScreen(
            onSongClick = { _, _, _ -> },
            onPlaylistClick = {},
            onCreatePlaylist = {},
            onNavigateToPremium = {},
            onLogout = {},
        )
    }
}

@Preview(name = "Home • Light", showBackground = true)
@Composable
private fun HomeScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        HomeScreen(
            onSongClick = { _, _, _ -> },
            onPlaylistClick = {},
            onCreatePlaylist = {},
            onNavigateToPremium = {},
            onLogout = {},
        )
    }
}
