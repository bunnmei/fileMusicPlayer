package space.webkombinat.musicplayer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import space.webkombinat.musicplayer.ui.library.AlbumDetailScreen
import space.webkombinat.musicplayer.ui.library.ArtistDetailScreen
import space.webkombinat.musicplayer.ui.library.LibraryScreen
import space.webkombinat.musicplayer.ui.navigation.AlbumRoute
import space.webkombinat.musicplayer.ui.navigation.ArtistRoute
import space.webkombinat.musicplayer.ui.navigation.FoldersRoute
import space.webkombinat.musicplayer.ui.navigation.LibraryRoute
import space.webkombinat.musicplayer.ui.navigation.NowPlayingRoute
import space.webkombinat.musicplayer.ui.navigation.SettingsRoute
import space.webkombinat.musicplayer.ui.permission.PermissionGate
import space.webkombinat.musicplayer.ui.player.MiniPlayer
import space.webkombinat.musicplayer.ui.player.NowPlayingScreen
import space.webkombinat.musicplayer.ui.player.PlayerViewModel
import space.webkombinat.musicplayer.ui.settings.FoldersScreen
import space.webkombinat.musicplayer.ui.settings.SettingsScreen

/** ミニプレイヤーに隠れないよう、リストの下に空ける高さ */
val LocalBottomContentPadding = compositionLocalOf { 0.dp }

private val MiniPlayerSpace: Dp = 88.dp

@Composable
fun MusicPlayerApp(appViewModel: AppViewModel = hiltViewModel()) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        PermissionGate(onGranted = appViewModel::onPermissionGranted) {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val player by playerViewModel.uiState.collectAsStateWithLifecycle()
    val position by playerViewModel.positionMs.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val onNowPlaying = backStackEntry?.destination?.hasRoute<NowPlayingRoute>() == true
    val showMiniPlayer = player.playback.hasMedia && !onNowPlaying

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalBottomContentPadding provides if (player.playback.hasMedia) MiniPlayerSpace else 0.dp) {
            NavHost(
                navController = navController,
                startDestination = LibraryRoute,
                enterTransition = { slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(200)) },
            ) {
                composable<LibraryRoute> {
                    LibraryScreen(
                        onAlbumClick = { navController.navigate(AlbumRoute(it.id)) },
                        onArtistClick = { navController.navigate(ArtistRoute(it.name)) },
                        onSettingsClick = { navController.navigate(SettingsRoute) },
                        playerViewModel = playerViewModel,
                    )
                }
                composable<AlbumRoute> {
                    AlbumDetailScreen(
                        onBack = navController::navigateUp,
                        playerViewModel = playerViewModel,
                    )
                }
                composable<ArtistRoute> {
                    ArtistDetailScreen(
                        onBack = navController::navigateUp,
                        onAlbumClick = { navController.navigate(AlbumRoute(it.id)) },
                        playerViewModel = playerViewModel,
                    )
                }
                composable<SettingsRoute> {
                    SettingsScreen(
                        onBack = navController::navigateUp,
                        onFoldersClick = { navController.navigate(FoldersRoute) },
                    )
                }
                composable<FoldersRoute> {
                    FoldersScreen(onBack = navController::navigateUp)
                }
                composable<NowPlayingRoute>(
                    enterTransition = { slideInVertically(tween(350)) { it } },
                    exitTransition = { fadeOut(tween(200)) },
                    popEnterTransition = { fadeIn(tween(200)) },
                    popExitTransition = { slideOutVertically(tween(300)) { it } },
                ) {
                    NowPlayingScreen(
                        state = player,
                        positionMs = position,
                        onClose = navController::navigateUp,
                        viewModel = playerViewModel,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showMiniPlayer,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        ) {
            MiniPlayer(
                state = player,
                positionMs = position,
                onClick = { navController.navigate(NowPlayingRoute) { launchSingleTop = true } },
                onPlayPause = playerViewModel::togglePlayPause,
                onNext = playerViewModel::next,
            )
        }
    }
}
