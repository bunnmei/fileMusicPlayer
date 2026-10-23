package space.webkombinat.musicplayer.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import space.webkombinat.musicplayer.data.LibraryState
import space.webkombinat.musicplayer.data.MusicRepository
import space.webkombinat.musicplayer.data.model.Library
import javax.inject.Inject

data class LibraryUiState(
    val loading: Boolean = true,
    /** 検索で絞り込んだ結果 */
    val library: Library = Library(),
    /** 端末にある曲の総数（0 なら「曲が見つかりません」を出す） */
    val totalSongs: Int = 0,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<LibraryUiState> = combine(repository.state, _query) { state, query ->
        when (state) {
            LibraryState.Loading -> LibraryUiState(loading = true)
            is LibraryState.Ready -> LibraryUiState(
                loading = false,
                library = state.library.filter(query.trim()),
                totalSongs = state.library.songs.size,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun setQuery(query: String) {
        _query.value = query
    }

    private fun Library.filter(query: String): Library {
        if (query.isEmpty()) return this
        fun String?.hit() = this?.contains(query, ignoreCase = true) == true
        return Library(
            songs = songs.filter { it.title.hit() || it.artist.hit() || it.album.hit() },
            albums = albums.filter { it.title.hit() || it.artist.hit() },
            artists = artists.filter { it.name.hit() },
        )
    }
}
