package space.webkombinat.musicplayer.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.webkombinat.musicplayer.data.MusicRepository
import space.webkombinat.musicplayer.playback.PlayerController
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerController: PlayerController,
) : ViewModel() {

    /** 権限が得られたらライブラリの読み込みと再生サービスへの接続を始める */
    fun onPermissionGranted() {
        repository.start()
        playerController.connect()
    }
}
