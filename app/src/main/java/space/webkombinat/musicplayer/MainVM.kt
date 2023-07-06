package space.webkombinat.musicplayer

import android.os.Environment
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.io.File

class MainVM: ViewModel(){
//    val path = Environment.getExternalStorageDirectory().path
//    //特定のフォルダのみを表示する。なければ作るように指示
//    val rootDir = File(path + "/Music/Records").listFiles()
//    private var _status = mutableStateOf("")
//    val status = _status
//
//    fun makeFolderList(): Array<File>? {
//        if (rootDir == null){
//            _status.value = "内部ストレージ＞Music＞Records　フォルダを作成してください"
//            return
//        }
//    }
}
// permissionがないAudio
// permissionがないMedia
// RecordsFolderがないがない
// Ok
