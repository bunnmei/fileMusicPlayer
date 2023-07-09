package space.webkombinat.musicplayer.components

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.webkombinat.musicplayer.MAndI
import space.webkombinat.musicplayer.MainVM


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    openBottomSheet: MutableState<Boolean>,
    bottomSheetState: SheetState,
    vm: MainVM,
    modifier: Modifier = Modifier,
) {
    val playOrStop = vm.musicState.value
    val mp = vm.musicPosi
    val mpm = vm.musicMax
    val meta = vm.musicMeta
    if(openBottomSheet.value){
        BoxWithConstraints {
            val screenWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
            val screenHeight = with(LocalDensity.current) { constraints.maxHeight.toDp() }
            ModalBottomSheet(
                shape = RoundedCornerShape(0.dp),
                onDismissRequest = { openBottomSheet.value = false },
                sheetState = bottomSheetState,
                windowInsets = WindowInsets(0),
                dragHandle = {
                    Box(
                        modifier = modifier
                            .fillMaxWidth()
                            .height(screenHeight / 5 * 3),
                        contentAlignment = Alignment.Center
                    ) {
//                      画像
                        Box(
                            modifier = modifier
                                .width(screenWidth - 100.dp)
                                .aspectRatio(1f / 1f)
                                .background(Color.Green)
                        ){
                             if (meta.value?.ipath != null){
                                Image(
                                    bitmap = BitmapFactory.decodeFile(meta.value!!.ipath).asImageBitmap(),
                                    contentDescription = null,
                                    modifier = modifier.fillMaxSize()
                                )
                             }
                        }
                    }
                }
            ) {

                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = modifier
                            .width(screenWidth - 100.dp)
                    ) {
                        if (meta.value != null){
                            val mmr = MediaMetadataRetriever()
                            mmr.setDataSource(meta.value!!.mpaht)
                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let {
                                Text(
                                    text = it,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                )
                            }
                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let {
                                Text(
                                    text = it,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                )
                            }
                        }
                        Spacer(modifier = modifier.height(20.dp))
                        Slider(
                            modifier = modifier,
                            value = mp.value.toFloat(),
                            onValueChange = { sliderValue ->
                               vm.setSlider(sliderValue)
                            },
                            onValueChangeFinished = {
                                vm.setSliderFinish()
                            },
                            valueRange = 0f..mpm.value.toFloat()
                        )
                        Spacer(modifier = modifier.height(20.dp))
                        Row(
                            modifier = modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = modifier
                                    .height(75.dp)
                                    .width(75.dp)
                                    .background(Color.Green, RoundedCornerShape(50))
                                    .clickable {
                                               vm.prevMusic()
                                    }
                                ,
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "prev-music",
                                    modifier = modifier
                                        .width(50.dp)
                                        .height(70.dp)
                                )
                            }

                            Box(
                                modifier = modifier
                                    .height(75.dp)
                                    .width(75.dp)
                                    .background(Color.Green, RoundedCornerShape(50))
                                    .clickable {
                                        vm.stopOrStart()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (playOrStop)
                                        Icons.Default.Pause
                                    else
                                        Icons.Default.PlayArrow,
                                    contentDescription = "play-stop-music",
                                    modifier = modifier
                                        .width(50.dp)
                                        .height(70.dp)
                                )
                            }

                            Box(
                                modifier = modifier
                                    .height(75.dp)
                                    .width(75.dp)
                                    .background(Color.Green, RoundedCornerShape(50))
                                    .clickable {
                                        vm.nextMusic()
                                    }
                                ,
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "next-music",
                                    modifier = modifier
                                        .width(50.dp)
                                        .height(70.dp)
                                )
                            }
                        }
                    }

                }

            }
        }
    }
}