package space.webkombinat.musicplayer.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun Folder(img:List<String>?, musics: List<String>?, modifier: Modifier = Modifier) {
    if (musics != null) {
        val bitmap: Bitmap?
        if (img != null){
            if (img.isNotEmpty()){
                bitmap = BitmapFactory.decodeFile(img[0])
//                Image(
//                    bitmap = bitmap.asImageBitmap(),
//                    contentDescription = null,
//                )
            }
        }
        val MMR = MediaMetadataRetriever()
        musics.forEach {
            MMR.setDataSource(it)
            Text(text = MMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "?")
            Text(text = MMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "?")
        }
        Divider(modifier = modifier.height(10.dp))
    }
}