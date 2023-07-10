package space.webkombinat.musicplayer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Snackbar(
    snackState: MutableState<Boolean>,
    text: String?,
    onClose:()-> Unit,
    onOpen:()-> Unit,
    modifier: Modifier = Modifier
) {
    val px = LocalDensity.current.run { 60.dp.toPx() }
    AnimatedVisibility(
        visible = snackState.value,
        enter = slideIn(
            initialOffset = {
                IntOffset( 0, px.toInt() )
            },
            animationSpec = tween(150)
        ),
        exit = slideOut(
            targetOffset = {
                IntOffset( 0, px.toInt() )
            },
            animationSpec = tween(150)
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.Magenta)
                .clickable { onOpen() }
        ) {
            Box(modifier = modifier
                .fillMaxHeight()
                .weight(1f),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = text?.substring(2) ?: "????",
                    fontSize = 24.sp,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = modifier
                    .fillMaxHeight()
                    .width(60.dp)
                    .clickable {
                               onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "close")
            }
        }
    }
}