package space.webkombinat.musicplayer.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun Folder(img:String?, music: String?, modifier: Modifier = Modifier,id: Int, onClick:(Int) -> Unit) {
    if (music != null) {
        val bitmap: Bitmap? =
        if (img != null){
            if (img.isNotEmpty()){
                 BitmapFactory.decodeFile(img)
            } else {
                null
            }
        } else {
             null
        }
//      mp3のID3を取得するため
        val MMR = MediaMetadataRetriever()
        MMR.setDataSource(music)
        FolderPanel(MMR = MMR, bitmap = bitmap){
            onClick(id)
        }
    }
}

@Composable
fun FolderPanel(MMR: MediaMetadataRetriever, bitmap: Bitmap?, modifier: Modifier = Modifier, onClick:() -> Unit) {
    Row(
        modifier = modifier
            .height(80.dp)
            .fillMaxWidth()
            .clickable {
                       onClick()
                       Log.i("MSG", "clickMSG")
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // thumbnail

        Box(
            modifier = modifier
                .height(80.dp)
                .width(80.dp)
        ){
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                )
            }
        }
        // description text
        Column(
            modifier = modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            Text(
                text = MMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.substring(2) ?: "?",
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                maxLines = 2
            )
            Text(
                text = MMR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "?",
                color = Color.Black.copy(0.7f),
                maxLines = 1
            )
        }

        Box(
            modifier = modifier
                .height(80.dp)
                .width(50.dp)
                .clickable {
                    Log.i("MSG", "click-reader")
                },
            contentAlignment = Alignment.Center
        ){
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "tenntenn")
        }
        
    }
}