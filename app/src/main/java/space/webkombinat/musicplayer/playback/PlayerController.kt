package space.webkombinat.musicplayer.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import space.webkombinat.musicplayer.data.model.Song
import javax.inject.Inject
import javax.inject.Singleton

data class QueueItem(val mediaId: String, val title: String, val artist: String?, val artworkUri: Uri?)

data class PlaybackState(
    val currentMediaId: String? = null,
    val title: String = "",
    val artist: String? = null,
    val artworkUri: Uri? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val queue: List<QueueItem> = emptyList(),
    val currentIndex: Int = -1,
) {
    val hasMedia: Boolean get() = currentMediaId != null
}

/**
 * UI から再生サービスを操作するための窓口。
 * MediaController の状態を StateFlow に変換して Compose から扱いやすくする。
 */
@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private var positionJob: Job? = null

    private val controller: Deferred<MediaController> by lazy {
        scope.async {
            val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            MediaController.Builder(context, token).buildAsync().await().also { controller ->
                controller.addListener(object : Player.Listener {
                    override fun onEvents(player: Player, events: Player.Events) = sync(player)
                })
                sync(controller)
            }
        }
    }

    /** サービスに接続しておく（再生中の曲を画面に出すため、起動時に呼ぶ） */
    fun connect() {
        controller.start()
    }

    fun play(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) = withController {
        if (songs.isEmpty()) return@withController
        val start = if (shuffle) songs.indices.random() else startIndex.coerceIn(songs.indices)
        shuffleModeEnabled = shuffle
        setMediaItems(songs.map { it.toMediaItem() }, start, 0L)
        prepare()
        play()
    }

    fun togglePlayPause() = withController {
        if (isPlaying) {
            pause()
        } else {
            if (playbackState == Player.STATE_ENDED) seekToDefaultPosition()
            play()
        }
    }

    fun next() = withController { seekToNext() }

    /** 3 秒以上再生していれば曲の頭に、そうでなければ前の曲に戻る */
    fun previous() = withController { seekToPrevious() }

    fun seekTo(positionMs: Long) = withController {
        seekTo(positionMs)
        _positionMs.value = positionMs
    }

    fun skipToQueueItem(index: Int) = withController {
        seekToDefaultPosition(index)
        play()
    }

    fun toggleShuffle() = withController { shuffleModeEnabled = !shuffleModeEnabled }

    fun cycleRepeatMode() = withController {
        repeatMode = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun stop() = withController {
        stop()
        clearMediaItems()
    }

    private fun withController(block: MediaController.() -> Unit) {
        scope.launch { controller.await().block() }
    }

    private fun sync(player: Player) {
        val metadata = player.mediaMetadata
        _state.update {
            it.copy(
                currentMediaId = player.currentMediaItem?.mediaId,
                title = metadata.title?.toString().orEmpty(),
                artist = metadata.artist?.toString(),
                artworkUri = metadata.artworkUri,
                isPlaying = player.isPlaying,
                durationMs = player.duration.coerceAtLeast(0L),
                shuffle = player.shuffleModeEnabled,
                repeatMode = player.repeatMode,
                queue = (0 until player.mediaItemCount).map { index ->
                    val item = player.getMediaItemAt(index)
                    QueueItem(
                        mediaId = item.mediaId,
                        title = item.mediaMetadata.title?.toString().orEmpty(),
                        artist = item.mediaMetadata.artist?.toString(),
                        artworkUri = item.mediaMetadata.artworkUri,
                    )
                },
                currentIndex = player.currentMediaItemIndex,
            )
        }
        _positionMs.value = player.currentPosition
        updatePositionPolling(player)
    }

    private fun updatePositionPolling(player: Player) {
        if (player.isPlaying) {
            if (positionJob?.isActive == true) return
            positionJob = scope.launch {
                while (isActive) {
                    _positionMs.value = player.currentPosition
                    delay(250)
                }
            }
        } else {
            positionJob?.cancel()
            positionJob = null
        }
    }
}

private fun Song.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id.toString())
    .setUri(uri)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setAlbumArtist(albumArtist)
            .setTrackNumber(trackNumber.takeIf { it > 0 })
            // 通知用のジャケットは、確実に存在するフォルダ画像を優先する
            .setArtworkUri(artworkUris.getOrNull(1) ?: artworkUris.firstOrNull())
            .setIsPlayable(true)
            .setIsBrowsable(false)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .build(),
    )
    .build()
