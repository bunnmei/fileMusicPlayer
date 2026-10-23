package space.webkombinat.musicplayer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOff
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import space.webkombinat.musicplayer.R
import space.webkombinat.musicplayer.ui.LocalBottomContentPadding
import space.webkombinat.musicplayer.ui.components.CenteredMessage

/** 曲が入っているフォルダをツリーで並べ、ライブラリに表示するかを選ぶ画面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(onBack: () -> Unit, viewModel: FoldersViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.folders)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + LocalBottomContentPadding.current + 8.dp,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Text(
                    text = stringResource(R.string.folders_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
            if (items.isEmpty()) {
                item { CenteredMessage(title = stringResource(R.string.folders_empty), modifier = Modifier.fillMaxWidth()) }
            }
            items(items, key = { it.node.path }) { item ->
                FolderRow(item = item, onToggle = { viewModel.setShown(item.node.path, it) })
            }
        }
    }
}

@Composable
private fun FolderRow(item: FolderItem, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = item.shown,
                enabled = !item.hiddenByParent,
                role = Role.Checkbox,
                onValueChange = onToggle,
            )
            .padding(start = 20.dp + 20.dp * item.node.depth, end = 12.dp, top = 6.dp, bottom = 6.dp)
            .alpha(if (item.hiddenByParent) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (item.shown) Icons.Rounded.Folder else Icons.Rounded.FolderOff,
            contentDescription = null,
            tint = if (item.shown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.node.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = pluralStringResource(R.plurals.song_count, item.node.songCount, item.node.songCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // 行全体で切り替えるので、チェックボックス自体はクリックを受けない
        Checkbox(checked = item.shown, onCheckedChange = null, enabled = !item.hiddenByParent)
    }
}
