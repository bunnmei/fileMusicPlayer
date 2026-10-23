package space.webkombinat.musicplayer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import space.webkombinat.musicplayer.data.FolderNode
import space.webkombinat.musicplayer.data.MusicRepository
import space.webkombinat.musicplayer.data.SettingsRepository
import javax.inject.Inject

data class FolderItem(
    val node: FolderNode,
    /** ライブラリに表示するか */
    val shown: Boolean,
    /** 親フォルダが非表示のため、このフォルダは切り替えられない */
    val hiddenByParent: Boolean,
)

@HiltViewModel
class FoldersViewModel @Inject constructor(
    musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val items: StateFlow<List<FolderItem>> =
        combine(musicRepository.folders, settingsRepository.settings) { folders, settings ->
            val excluded = settings.excludedFolders
            folders.map { node ->
                val hiddenByParent = excluded.any { it != node.path && node.path.startsWith(it) }
                FolderItem(node = node, shown = !hiddenByParent && node.path !in excluded, hiddenByParent = hiddenByParent)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setShown(path: String, shown: Boolean) {
        viewModelScope.launch { settingsRepository.setFolderExcluded(path, excluded = !shown) }
    }
}
