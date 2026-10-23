package space.webkombinat.musicplayer.data

import android.content.Context
import android.database.ContentObserver
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import space.webkombinat.musicplayer.data.model.Library
import space.webkombinat.musicplayer.data.model.Song
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

sealed interface LibraryState {
    data object Loading : LibraryState
    data class Ready(val library: Library) : LibraryState
}

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    settingsRepository: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val source = MediaStoreSource(context.contentResolver)

    /** 端末内の全曲（除外フォルダも含む）。読み込み前は null */
    private val allSongs = MutableStateFlow<List<Song>?>(null)

    private val excludedFolders = settingsRepository.settings.map { it.excludedFolders }.distinctUntilChanged()

    /** 除外フォルダを取り除いたライブラリ */
    val state: StateFlow<LibraryState> = combine(allSongs, excludedFolders) { songs, excluded ->
        if (songs == null) {
            LibraryState.Loading
        } else {
            LibraryState.Ready(LibraryBuilder.build(songs.filterNot { LibraryBuilder.isExcluded(it.folder, excluded) }))
        }
    }.stateIn(scope, SharingStarted.Eagerly, LibraryState.Loading)

    /** 曲が入っているフォルダの一覧（除外フォルダも含む）。「表示するフォルダ」画面用 */
    val folders: StateFlow<List<FolderNode>> = allSongs
        .map { songs -> FolderTree.build(songs.orEmpty().groupingBy { it.folder }.eachCount()) }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** 今読み込まれているライブラリ（読み込み前は空） */
    val library: Library get() = (state.value as? LibraryState.Ready)?.library ?: Library()

    private var started = false
    private var reloadJob: Job? = null

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            // ファイルの追加・削除はまとめて通知されるので少し待ってから読み直す
            reloadJob?.cancel()
            reloadJob = scope.launch {
                delay(1_000)
                reload()
            }
        }
    }

    /** 読み取り権限を得たあとに呼ぶ。MediaStore の変更も監視し始める */
    fun start() {
        if (started) return
        started = true
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer,
        )
        scope.launch { reload() }
    }

    suspend fun reload() = withContext(Dispatchers.IO) {
        try {
            allSongs.value = source.loadSongs()
        } catch (e: SecurityException) {
            Log.w(TAG, "No permission to read media", e)
        }
    }

    /**
     * Music フォルダを走査して MediaStore に再登録させる。
     * PC からコピーした直後など、曲がまだ一覧に出てこないときに使う。
     */
    suspend fun rescan(): Int {
        val files = withContext(Dispatchers.IO) {
            val music = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            music.walkTopDown()
                .onFail { _, _ -> }
                .filter { it.isFile && !it.name.startsWith(".") }
                .map(File::getAbsolutePath)
                .toList()
        }
        if (files.isNotEmpty()) scanFiles(files)
        reload()
        return files.size
    }

    private suspend fun scanFiles(paths: List<String>) = suspendCancellableCoroutine { cont ->
        val remaining = AtomicInteger(paths.size)
        MediaScannerConnection.scanFile(context, paths.toTypedArray(), null) { _, _ ->
            if (remaining.decrementAndGet() == 0 && cont.isActive) cont.resume(Unit)
        }
    }

    private companion object {
        const val TAG = "MusicRepository"
    }
}
