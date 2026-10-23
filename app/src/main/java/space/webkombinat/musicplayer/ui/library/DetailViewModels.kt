package space.webkombinat.musicplayer.ui.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import space.webkombinat.musicplayer.data.LibraryState
import space.webkombinat.musicplayer.data.MusicRepository
import space.webkombinat.musicplayer.data.model.Album
import space.webkombinat.musicplayer.data.model.Artist
import space.webkombinat.musicplayer.ui.navigation.AlbumRoute
import space.webkombinat.musicplayer.ui.navigation.ArtistRoute
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: MusicRepository,
) : ViewModel() {
    private val albumId = savedStateHandle.toRoute<AlbumRoute>().albumId

    val album: StateFlow<Album?> = repository.state
        .map { (it as? LibraryState.Ready)?.library?.album(albumId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.library.album(albumId))
}

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: MusicRepository,
) : ViewModel() {
    private val name = savedStateHandle.toRoute<ArtistRoute>().name

    val artist: StateFlow<Artist?> = repository.state
        .map { (it as? LibraryState.Ready)?.library?.artist(name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.library.artist(name))
}
