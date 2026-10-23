package space.webkombinat.musicplayer.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import space.webkombinat.musicplayer.R
import space.webkombinat.musicplayer.data.model.Album
import space.webkombinat.musicplayer.ui.LocalBottomContentPadding
import space.webkombinat.musicplayer.ui.components.AlbumArt
import space.webkombinat.musicplayer.ui.components.AlbumCard
import space.webkombinat.musicplayer.ui.components.SongRow
import space.webkombinat.musicplayer.ui.components.artistLabel
import space.webkombinat.musicplayer.ui.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    playerViewModel: PlayerViewModel,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
) {
    val artist by viewModel.artist.collectAsStateWithLifecycle()
    val player by playerViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        val current = artist ?: return@Scaffold
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + LocalBottomContentPadding.current + 8.dp,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AlbumArt(uris = current.artworkUris, shape = CircleShape, modifier = Modifier.size(144.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        artistLabel(current.name),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Text(
                        text = pluralStringResource(R.plurals.album_count, current.albums.size, current.albums.size) +
                            " · " + pluralStringResource(R.plurals.song_count, current.songs.size, current.songs.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                PlayShuffleButtons(
                    onPlay = { playerViewModel.play(current.songs, 0) },
                    onShuffle = { playerViewModel.shuffle(current.songs) },
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
            item { SectionTitle(stringResource(R.string.tab_albums)) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 10.dp)) {
                    items(current.albums, key = { it.id }) { album ->
                        AlbumCard(album = album, onClick = { onAlbumClick(album) }, modifier = Modifier.width(156.dp))
                    }
                }
            }
            item { SectionTitle(stringResource(R.string.tab_songs)) }
            itemsIndexed(current.songs, key = { _, song -> song.id }) { index, song ->
                SongRow(
                    song = song,
                    isCurrent = song.id.toString() == player.playback.currentMediaId,
                    onClick = { playerViewModel.play(current.songs, index) },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
    )
}
