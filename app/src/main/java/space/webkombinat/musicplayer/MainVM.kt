package space.webkombinat.musicplayer

import android.media.MediaPlayer
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainVM: ViewModel(){
    private var _musicState = mutableStateOf(false)
    val musicState = _musicState

    private var _musicPosi = mutableStateOf(0)
    val musicPosi = _musicPosi

    private var _musicPath: MutableState<String?> = mutableStateOf(null)
    val musicPath = _musicPath

    private var mediaPlayer: MutableState<MediaPlayer?> = mutableStateOf(null)
    val mp = mediaPlayer


    fun setPath(path: String){
        if (mediaPlayer.value != null){
            mediaPlayer.value!!.release()
            mediaPlayer.value = null
        }
        _musicPath.value = path
        playMusic()
    }
    private fun playMusic(){
        mediaPlayer.value = MediaPlayer()
        mediaPlayer.value!!.setDataSource(_musicPath.value)
        mediaPlayer.value!!.prepareAsync()
        mediaPlayer.value!!.setOnPreparedListener { mp ->

            mp.start()
        }
//        viewModelScope.launch(Dispatchers.IO) {
//            while (mediaPlayer != null){
//                _musicPosi.value = mediaPlayer!!.currentPosition
//                Log.i("MSG", mediaPlayer!!.currentPosition.toString())
//                delay(1000)
//            }
//        }


        musicState.value = true

    }
    fun stopOrStart(){
        if (_musicState.value){
            stopMusic()
            _musicState.value = false
        } else {
            startMusic()
            _musicState.value = true
        }
    }
    fun startMusic(){
        mediaPlayer.value?.start()
    }
    fun stopMusic(){
        mediaPlayer.value?.pause()
    }
}
//            _status.value = "内部ストレージ＞Music＞Records　フォルダを作成してください"
// permissionがないAudio
// permissionがないMedia
// RecordsFolderがないがない
// Ok
