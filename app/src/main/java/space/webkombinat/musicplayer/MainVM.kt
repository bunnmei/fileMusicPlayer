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
    private val path: String? = Environment.getExternalStorageDirectory().path
    //特定のフォルダのみを表示する。なければ作るように指示
    val rootDir: Array<File>? = File("$path/Music/Records").listFiles()

    val musicAndImageList = makeList(rootDir)

    private var _musicState = mutableStateOf(false)
    val musicState = _musicState

    private var _musicIsPlay = mutableStateOf(false)
    val musicIsPlay = _musicIsPlay

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

    private var _nextInt = mutableStateOf(0)

    fun setPath(i: Int){
        if (mediaPlayer.value != null){
            timer.cancel()
            mediaPlayer.value!!.release()
            mediaPlayer.value = null
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
        _musicState.value = true
        _musicIsPlay.value = true
    }
    fun stopOrStart(){
        if (_musicIsPlay.value){
            stopMusic()
            _musicIsPlay.value = false
        } else {
            startMusic()
            _musicIsPlay.value = true
        }
    }

    fun closeMusic(){
        if (mediaPlayer.value != null){
            timer.cancel()
            mediaPlayer.value!!.release()
            mediaPlayer.value = null
        }
        _musicIsPlay.value = false
        _musicState.value = false
    }
    fun startMusic(){
        mediaPlayer.value?.start()
        timerGetPosi()
    }

    fun stopMusic(){
        mediaPlayer.value?.pause()
        timer.cancel()
    }

    fun timerGetPosi() {
        timer = fixedRateTimer(period = 1000L){
            _musicPosi.value = mediaPlayer.value?.currentPosition ?: 0
            val dur = mediaPlayer.value?.duration
            val cur = mediaPlayer.value?.currentPosition
//            Log.i("DATA", "$dur duration")
//            Log.i("DATA", "$cur current")
//          曲が終わったら次の曲に行く処理
            if (dur != null && cur != null && cur != 0 && dur != 0){
                val rangeS = dur / 1000 - 1
                val rangeE = dur / 1000 + 1
                if (cur / 1000 in rangeS..rangeE){
                    _nextInt.value += 1
                    if (_nextInt.value <= 2){
                        timer.cancel()
                        _nextInt.value = 0
                        nextMusic()
                    }
                }
            }

        }
    }

    fun nextMusic(){
        val all = musicAndImageList.size - 1
        val current = _musicPath.value
        if (current != null) {
            if (current < all){
                setPath(current + 1)
            } else {
                setPath(0)
            }
        }
    }

    fun prevMusic() {
        val all = musicAndImageList.size - 1
        val current = _musicPath.value
        if (current != null) {
            if (current == 0){
                setPath(all)
            } else {
                setPath(current - 1)
            }
        }
    }

    fun setSliderFinish(){
        mediaPlayer.value?.seekTo(_musicPosi.value)
    }

    fun setSlider(changeVal: Float) {
        _musicPosi.value = changeVal.toInt()
    }

    fun makeList(listMap: Array<File>?) : MutableList<MAndI> {
        val data: MutableList<MAndI> = mutableListOf()

        listMap
            ?.sortedBy { it.name.substring(0, 2).toInt() }
            ?.forEach { file ->

//          フォルダ内に画像があればジャケット画像に使う
            val img = file.listFiles()
                ?.filter { it.name.endsWith(".jpg") }
                ?.map { it.path }

//          mp3を拾い上げる
            val music = file.listFiles()
                ?.filter { it.name.endsWith(".mp3") }
                ?.sortedBy { it.name.substring(0, 2).toInt() }
                ?.map { it ->
                    it.path
                }

            music?.forEach { path ->
                data.add(MAndI(mpaht = path, ipath = img?.get(0)))
            }
        }

        return data
    }
}

data class MAndI (
    val mpaht: String,
    val ipath: String?
)

// _status.value = "内部ストレージ＞Music＞Records　フォルダを作成してください"
// permissionがないAudio
// permissionがないMedia
// RecordsFolderがないがない
// Ok
