package space.webkombinat.musicplayer.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import space.webkombinat.musicplayer.R
import space.webkombinat.musicplayer.data.model.Album
import space.webkombinat.musicplayer.ui.LocalBottomContentPadding
import space.webkombinat.musicplayer.ui.components.SquareAlbumArt
import space.webkombinat.musicplayer.ui.components.TrackRow
import space.webkombinat.musicplayer.ui.components.artistLabel
import space.webkombinat.musicplayer.ui.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    onBack: () -> Unit,
    playerViewModel: PlayerViewModel,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val album by viewModel.album.collectAsStateWithLifecycle()
    val player by playerViewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    // スクロールしてヘッダーが隠れたらタイトルを出す
                    if (scrollBehavior.state.contentOffset < -400f) {
                        Text(album?.title.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        val current = album ?: return@Scaffold
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + LocalBottomContentPadding.current + 8.dp,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { AlbumHeader(current) }
            item {
                PlayShuffleButtons(
                    onPlay = { playerViewModel.play(current.songs, 0) },
                    onShuffle = { playerViewModel.shuffle(current.songs) },
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
            itemsIndexed(current.songs, key = { _, song -> song.id }) { index, song ->
                TrackRow(
                    song = song,
                    showArtist = song.artist != null && song.artist != current.artist,
                    isCurrent = song.id.toString() == player.playback.currentMediaId,
                    onClick = { playerViewModel.play(current.songs, index) },
                )
            }
        }
    }
}

@Composable
private fun AlbumHeader(album: Album) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SquareAlbumArt(
            uris = album.artworkUris,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .fillMaxWidth(0.72f)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = album.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = artistLabel(album.artist),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        val meta = buildList {
            if (album.year > 0) add(album.year.toString())
            add(pluralStringResource(R.plurals.song_count, album.songs.size, album.songs.size))
            add(stringResource(R.string.minutes, (album.durationMs / 60_000).toInt().coerceAtLeast(1)))
        }
        Text(
            text = meta.joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
