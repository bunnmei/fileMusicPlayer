package space.webkombinat.musicplayer.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import space.webkombinat.musicplayer.data.MusicRepository
import space.webkombinat.musicplayer.data.model.Song
import space.webkombinat.musicplayer.playback.PlaybackState
import space.webkombinat.musicplayer.playback.PlayerController
import javax.inject.Inject

data class PlayerUiState(
    val playback: PlaybackState = PlaybackState(),
    /** 再生中の曲（ライブラリから見つからないときは null） */
    val song: Song? = null,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlayerController,
    repository: MusicRepository,
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = combine(controller.state, repository.state) { playback, _ ->
        PlayerUiState(
            playback = playback,
            song = playback.currentMediaId?.toLongOrNull()?.let { repository.library.song(it) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerUiState())

    val positionMs: StateFlow<Long> = controller.positionMs

    fun play(songs: List<Song>, startIndex: Int = 0) = controller.play(songs, startIndex)
    fun shuffle(songs: List<Song>) = controller.play(songs, shuffle = true)
    fun togglePlayPause() = controller.togglePlayPause()
    fun next() = controller.next()
    fun previous() = controller.previous()
    fun seekTo(positionMs: Long) = controller.seekTo(positionMs)
    fun skipToQueueItem(index: Int) = controller.skipToQueueItem(index)
    fun toggleShuffle() = controller.toggleShuffle()
    fun cycleRepeatMode() = controller.cycleRepeatMode()
}
