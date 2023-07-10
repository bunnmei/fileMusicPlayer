package space.webkombinat.musicplayer

import android.Manifest
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import space.webkombinat.musicplayer.components.BottomSheet
import space.webkombinat.musicplayer.components.Folder
import space.webkombinat.musicplayer.components.Snackbar
import space.webkombinat.musicplayer.ui.theme.MusicPlayerTheme
import java.io.File

// Permissionは↓を参考
// https://github.com/philipplackner/PermissionsGuideCompose/blob/master/app/src/main/java/com/plcoding/permissionsguidecompose/MainActivity.kt

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionsToRequest = arrayOf(
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val visiblePermissionDialogQueue = mutableStateListOf<String>()

        fun onPermissionResult(
            permission: String,
            isGranted: Boolean
        ) {
            if(!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
                visiblePermissionDialogQueue.add(permission)
            }
        }
        setContent {
            MusicPlayerTheme {
                val vm = viewModel<MainVM>()
                val meta = vm.musicMeta

                val multiplePermissionResultLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { perms ->
                            permissionsToRequest.forEach { permission ->
                                onPermissionResult(
                                    permission = permission,
                                    isGranted = perms[permission] == true
                                )
                            }
                        }
                    )

                LaunchedEffect(Unit){
                    multiplePermissionResultLauncher.launch(permissionsToRequest)
                }

                val openBottomSheet = rememberSaveable { mutableStateOf(false) }
//              val scope = rememberCoroutineScope()
//              version番号あげないとrememberModalBottomSheetStateは使えない
                val bottomSheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )

                val rootDir = vm.rootDir
                val data = vm.musicAndImageList
                if (rootDir == null){
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "内部ストレージ＞Music＞Records \n フォルダを作成してください")
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()){
                        LazyColumn {
                            itemsIndexed(
                                items = data,
                            ){ index ,mi ->
    
                                Folder(img = mi.ipath, music = mi.mpaht, id = index){ path ->
                                    vm.setPath(path)
                                    openBottomSheet.value = true
                                }
    
                            }
                        }
                        
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val mmr = MediaMetadataRetriever()
                            if (meta.value != null){
                                mmr.setDataSource(meta.value!!.mpaht)
                            }
                            Snackbar(
                                snackState = vm.musicState,
                                text = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                                onClose = {
                                    vm.closeMusic()
                                },
                                onOpen = {
                                    openBottomSheet.value = true
                                })
                        }
                    }

                    BottomSheet(openBottomSheet = openBottomSheet, bottomSheetState = bottomSheetState, vm = vm)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusicPlayerTheme {
        Greeting("Android")
    }
}