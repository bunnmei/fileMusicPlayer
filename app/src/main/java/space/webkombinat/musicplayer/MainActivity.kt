package space.webkombinat.musicplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import space.webkombinat.musicplayer.components.Folder
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

                val path = Environment.getExternalStorageDirectory().path
                //特定のフォルダのみを表示する。なければ作るように指示
                val rootDir = File(path + "/Music/Records").listFiles()
                if (rootDir == null){
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "内部ストレージ＞Music＞Records \n フォルダを作成してください")
                    }
                } else {
                    LazyColumn {
                        items(
                            items = rootDir,
                        ){ file ->
//                          CD or RECORD name ディレクトリ名がジャケット名
//                            if (file.isDirectory){
//                                Text(text = file.name)
//                            }

//                          mp3を拾い上げる
                            val musics = file.listFiles()
                                ?.filter( { it.name.endsWith(".mp3") })
                                ?.map({it.path})

//                          フォルダ内に画像があればジャケット画像に使う
                            val img = file.listFiles()
                                ?.filter( { it.name.endsWith(".jpeg") })
                                ?.map({it.path})
                            if (musics != null){
                                Folder(img = img, musics = musics){ path ->

                                }
                            }
                        }
                    }
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