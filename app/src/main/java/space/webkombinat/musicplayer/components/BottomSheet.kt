package space.webkombinat.musicplayer.components

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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val mp = vm.mp
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

                        }
                    }
                }
            ) {

                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
//                        .height(screenHeight / 5 * 2),
//                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = modifier
                            .width(screenWidth - 100.dp)
                    ) {
                        Text(
                            text = "NAME",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                        )
                        Text(
                            text = "namae",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                        )
                        Spacer(modifier = modifier.height(20.dp))
                        Slider(
                            modifier = modifier,
                            value = mp.value?.currentPosition?.toFloat() ?: 0f,
                            onValueChange = { sliderValue_ ->
    //                            viewModel.setFontNow(sliderValue_.toInt())
                            },
                            onValueChangeFinished = {
    //                            viewModel.setFontSize()
                            },
                            valueRange = if (mp.value != null) {0f..mp.value!!.duration.toFloat()} else {0f..100f}
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
                                    .background(Color.Green, RoundedCornerShape(50)),
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