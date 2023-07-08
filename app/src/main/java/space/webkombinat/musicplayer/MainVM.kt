package space.webkombinat.musicplayer

import android.media.MediaPlayer
import android.os.Environment
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.io.File

class MainVM: ViewModel(){

    private var _musicPath: MutableState<String?> = mutableStateOf(null)
    val musicPath = _musicPath
    private var mediaPlayer: MediaPlayer? = null
    fun setPath(path: String){
        if (mediaPlayer != null){
            mediaPlayer!!.release()
        }
        _musicPath.value = path
        playMusic()
    }
    private fun playMusic(){
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(_musicPath.value)
        mediaPlayer!!.prepare()
        mediaPlayer!!.start()
    }
}
//            _status.value = "内部ストレージ＞Music＞Records　フォルダを作成してください"
// permissionがないAudio
// permissionがないMedia
// RecordsFolderがないがない
// Ok
