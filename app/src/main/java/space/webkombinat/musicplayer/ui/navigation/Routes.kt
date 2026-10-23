package space.webkombinat.musicplayer.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LibraryRoute

@Serializable
data class AlbumRoute(val albumId: Long)

/** name が null のときは「不明なアーティスト」 */
@Serializable
data class ArtistRoute(val name: String?)

@Serializable
data object NowPlayingRoute

@Serializable
data object SettingsRoute

@Serializable
data object FoldersRoute
