package space.webkombinat.musicplayer.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * ジャケット画像。候補を先頭から順に読み込み、どれも無ければ音符アイコンを出す。
 */
@Composable
fun AlbumArt(
    uris: List<Uri>,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    var index by remember(uris) { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        val uri = uris.getOrNull(index)
        if (uri == null) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxSize(0.4f),
            )
        } else {
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { index++ },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** 正方形のジャケット（グリッド・詳細画面用） */
@Composable
fun SquareAlbumArt(uris: List<Uri>, modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(16.dp)) {
    AlbumArt(uris = uris, shape = shape, modifier = modifier.fillMaxWidth().aspectRatio(1f))
}
