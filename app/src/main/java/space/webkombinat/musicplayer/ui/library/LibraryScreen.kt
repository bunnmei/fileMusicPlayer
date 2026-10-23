package space.webkombinat.musicplayer.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import space.webkombinat.musicplayer.R
import space.webkombinat.musicplayer.data.model.Album
import space.webkombinat.musicplayer.data.model.Artist
import space.webkombinat.musicplayer.data.model.Library
import space.webkombinat.musicplayer.data.model.Song
import space.webkombinat.musicplayer.ui.LocalBottomContentPadding
import space.webkombinat.musicplayer.ui.components.AlbumCard
import space.webkombinat.musicplayer.ui.components.ArtistRow
import space.webkombinat.musicplayer.ui.components.CenteredMessage
import space.webkombinat.musicplayer.ui.components.SongRow
import space.webkombinat.musicplayer.ui.player.PlayerViewModel

private enum class LibraryTab(val label: Int) {
    Albums(R.string.tab_albums),
    Songs(R.string.tab_songs),
    Artists(R.string.tab_artists),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onSettingsClick: () -> Unit,
    playerViewModel: PlayerViewModel,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val player by playerViewModel.uiState.collectAsStateWithLifecycle()
    var searching by rememberSaveable { mutableStateOf(false) }

    val tabs = LibraryTab.entries
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            if (searching) {
                SearchTopBar(
                    query = query,
                    onQueryChange = viewModel::setQuery,
                    onClose = {
                        searching = false
                        viewModel.setQuery("")
                    },
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.library), style = MaterialTheme.typography.headlineSmall) },
                    actions = {
                        IconButton(onClick = { searching = true }) {
                            Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.search))
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings))
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage, containerColor = Color.Transparent) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(stringResource(tab.label)) },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.totalSongs == 0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CenteredMessage(
                        title = stringResource(R.string.empty_library_title),
                        body = stringResource(R.string.empty_library_body),
                    )
                }
                else -> HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    when (tabs[page]) {
                        LibraryTab.Albums -> AlbumGrid(state.library.albums, onAlbumClick)
                        LibraryTab.Songs -> SongList(
                            library = state.library,
                            currentMediaId = player.playback.currentMediaId,
                            onPlay = playerViewModel::play,
                            onShuffle = playerViewModel::shuffle,
                        )
                        LibraryTab.Artists -> ArtistList(state.library.artists, onArtistClick)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.close_search))
            }
        },
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.search_hint), maxLines = 1) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = null)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    )
}

@Composable
private fun bottomPadding() = PaddingValues(bottom = LocalBottomContentPadding.current + 8.dp)

@Composable
private fun AlbumGrid(albums: List<Album>, onAlbumClick: (Album) -> Unit) {
    if (albums.isEmpty()) return NoResults()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 156.dp),
        contentPadding = PaddingValues(
            start = 10.dp, end = 10.dp, top = 10.dp,
            bottom = LocalBottomContentPadding.current + 10.dp,
        ),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(albums, key = { it.id }) { album ->
            AlbumCard(album = album, onClick = { onAlbumClick(album) })
        }
    }
}

@Composable
private fun SongList(
    library: Library,
    currentMediaId: String?,
    onPlay: (List<Song>, Int) -> Unit,
    onShuffle: (List<Song>) -> Unit,
) {
    val songs = library.songs
    if (songs.isEmpty()) return NoResults()
    LazyColumn(contentPadding = bottomPadding(), modifier = Modifier.fillMaxSize()) {
        item {
            FilledTonalButton(
                onClick = { onShuffle(songs) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Icon(Icons.Rounded.Shuffle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.shuffle_all))
            }
        }
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            SongRow(
                song = song,
                isCurrent = song.id.toString() == currentMediaId,
                onClick = { onPlay(songs, index) },
            )
        }
    }
}

@Composable
private fun ArtistList(artists: List<Artist>, onArtistClick: (Artist) -> Unit) {
    if (artists.isEmpty()) return NoResults()
    LazyColumn(
        contentPadding = bottomPadding(),
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(artists, key = { it.name ?: "" }) { artist ->
            ArtistRow(artist = artist, onClick = { onArtistClick(artist) })
        }
    }
}

@Composable
private fun NoResults() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        CenteredMessage(title = stringResource(R.string.no_results))
    }
}
