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
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class MainVM: ViewModel(){
    val path = Environment.getExternalStorageDirectory().path
    //特定のフォルダのみを表示する。なければ作るように指示
    val rootDir = File(path + "/Music/Records").listFiles()

    val musicAndImageList = makeList(rootDir)

    private var _musicState = mutableStateOf(false)
    val musicState = _musicState

    private var _musicMax = mutableStateOf(0)
    val musicMax = _musicMax

    private var _musicPosi = mutableStateOf(0)
    val musicPosi = _musicPosi

    private var _musicPath: MutableState<Int?> = mutableStateOf(null)
    val musicPath = _musicPath

    private var mediaPlayer: MutableState<MediaPlayer?> = mutableStateOf(null)
    val mp = mediaPlayer

    private var _musicMeta:MutableState<MAndI?> = mutableStateOf(null)
    val musicMeta = _musicMeta

    private lateinit var timer: Timer

    fun setPath(i: Int){
        if (mediaPlayer.value != null){
            mediaPlayer.value!!.release()
            mediaPlayer.value = null
            timer.cancel()
        }
        _musicPath.value = i
        _musicMeta.value = musicAndImageList[i]
        playMusic()
    }
    private fun playMusic(){
        mediaPlayer.value = MediaPlayer()
        mediaPlayer.value!!.setDataSource(musicAndImageList[_musicPath.value!!].mpaht)
        mediaPlayer.value!!.prepareAsync()
        mediaPlayer.value!!.setOnPreparedListener { mp ->
            musicMax.value = mp.duration
            Log.i("Data", "${mp.duration}")
            mp.start()
        }
        timerGetPosi()
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

    fun timerGetPosi() {
        timer = fixedRateTimer(period = 1000L){
            _musicPosi.value = mediaPlayer.value?.currentPosition!!
            Log.i("Data", "${mediaPlayer.value?.currentPosition!!}")
        }
    }

    fun setSliderFinish(){
        mediaPlayer.value?.seekTo(_musicPosi.value)
    }

    fun setSlider(changeVal: Float) {
        _musicPosi.value = changeVal.toInt()
    }

    fun makeList(listMap: Array<File>) : MutableList<MAndI> {
        val data: MutableList<MAndI> = mutableListOf()

        listMap.forEach { file ->
//          フォルダ内に画像があればジャケット画像に使う
            val img = file.listFiles()
                ?.filter( { it.name.endsWith(".jpeg") })
                ?.map({it.path})
//          mp3を拾い上げる
            val music = file.listFiles()
                ?.filter( { it.name.endsWith(".mp3") })
                ?.map({it.path})

            music?.forEach { paht ->
                data.add(MAndI(mpaht = paht, ipath = img?.get(0)))
            }
        }

        return data
    }
}

data class MAndI (
    val mpaht: String,
    val ipath: String?
)

//            _status.value = "内部ストレージ＞Music＞Records　フォルダを作成してください"
// permissionがないAudio
// permissionがないMedia
// RecordsFolderがないがない
// Ok
