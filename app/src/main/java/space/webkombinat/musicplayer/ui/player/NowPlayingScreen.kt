package space.webkombinat.musicplayer.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import space.webkombinat.musicplayer.R
import space.webkombinat.musicplayer.playback.QueueItem
import space.webkombinat.musicplayer.ui.components.AlbumArt
import space.webkombinat.musicplayer.ui.components.SquareAlbumArt
import space.webkombinat.musicplayer.ui.components.artistLabel
import space.webkombinat.musicplayer.ui.components.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    state: PlayerUiState,
    positionMs: Long,
    onClose: () -> Unit,
    viewModel: PlayerViewModel,
) {
    val playback = state.playback
    var showQueue by rememberSaveable { mutableStateOf(false) }

    // 再生が終わってキューが空になったら閉じる
    LaunchedEffect(playback.hasMedia) { if (!playback.hasMedia) onClose() }

    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.primaryContainer, colors.surface, colors.surface)))
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = stringResource(R.string.close_player))
                }
                Text(
                    text = stringResource(R.string.now_playing),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = { showQueue = true }) {
                    Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = stringResource(R.string.queue))
                }
            }

            Spacer(Modifier.weight(1f))

            val artScale by animateFloatAsState(
                targetValue = if (playback.isPlaying) 1f else 0.86f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                label = "artScale",
            )
            SquareAlbumArt(
                uris = state.song?.artworkUris ?: listOfNotNull(playback.artworkUri),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .scale(artScale)
                    .shadow(24.dp, RoundedCornerShape(28.dp)),
            )

            Spacer(Modifier.weight(1f))

            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = state.song?.title ?: playback.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = artistLabel(state.song?.artist ?: playback.artist),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                state.song?.album?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SeekBar(positionMs = positionMs, durationMs = playback.durationMs, onSeek = viewModel::seekTo)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = viewModel::toggleShuffle) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = stringResource(R.string.shuffle),
                        tint = if (playback.shuffle) colors.primary else colors.onSurfaceVariant,
                    )
                }
                IconButton(onClick = viewModel::previous, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Rounded.SkipPrevious, stringResource(R.string.previous), Modifier.size(40.dp))
                }
                FilledIconButton(
                    onClick = viewModel::togglePlayPause,
                    shape = RoundedCornerShape(28.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.primary),
                    modifier = Modifier.size(84.dp),
                ) {
                    Icon(
                        imageVector = if (playback.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(if (playback.isPlaying) R.string.pause else R.string.play),
                        modifier = Modifier.size(44.dp),
                    )
                }
                IconButton(onClick = viewModel::next, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Rounded.SkipNext, stringResource(R.string.next), Modifier.size(40.dp))
                }
                IconButton(onClick = viewModel::cycleRepeatMode) {
                    val (icon, label) = when (playback.repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne to R.string.repeat_one
                        Player.REPEAT_MODE_ALL -> Icons.Rounded.Repeat to R.string.repeat_all
                        else -> Icons.Rounded.Repeat to R.string.repeat_off
                    }
                    Icon(
                        icon,
                        contentDescription = stringResource(label),
                        tint = if (playback.repeatMode != Player.REPEAT_MODE_OFF) colors.primary else colors.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showQueue) {
        ModalBottomSheet(onDismissRequest = { showQueue = false }) {
            QueueList(
                queue = playback.queue,
                currentIndex = playback.currentIndex,
                onItemClick = viewModel::skipToQueueItem,
            )
        }
    }
}

@Composable
private fun SeekBar(positionMs: Long, durationMs: Long, onSeek: (Long) -> Unit) {
    // ドラッグ中はつまみの位置を優先し、離したときにシークする
    var dragging by remember { mutableStateOf<Float?>(null) }
    val max = durationMs.coerceAtLeast(1L).toFloat()
    val value = dragging ?: positionMs.toFloat().coerceIn(0f, max)
    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = value,
            onValueChange = { dragging = it },
            onValueChangeFinished = {
                dragging?.let { onSeek(it.toLong()) }
                dragging = null
            },
            valueRange = 0f..max,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDuration(value.toLong()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatDuration(durationMs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QueueList(queue: List<QueueItem>, currentIndex: Int, onItemClick: (Int) -> Unit) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (currentIndex - 2).coerceAtLeast(0))
    Text(
        text = stringResource(R.string.queue),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(queue) { index, item ->
            val isCurrent = index == currentIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(index) }
                    .background(if (isCurrent) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlbumArt(uris = listOfNotNull(item.artworkUri), modifier = Modifier.size(44.dp), shape = RoundedCornerShape(8.dp))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        artistLabel(item.artist),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (isCurrent) {
                    Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
