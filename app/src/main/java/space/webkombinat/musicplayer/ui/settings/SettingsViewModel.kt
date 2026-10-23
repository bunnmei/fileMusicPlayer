package space.webkombinat.musicplayer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.webkombinat.musicplayer.data.AppSettings
import space.webkombinat.musicplayer.data.LibraryState
import space.webkombinat.musicplayer.data.MusicRepository
import space.webkombinat.musicplayer.data.SettingsRepository
import space.webkombinat.musicplayer.data.ThemeMode
import space.webkombinat.musicplayer.playback.AudioEffectsController
import space.webkombinat.musicplayer.playback.EqualizerCapabilities
import javax.inject.Inject

sealed interface RescanState {
    data object Idle : RescanState
    data object Running : RescanState
    data class Done(val fileCount: Int) : RescanState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val musicRepository: MusicRepository,
    private val effects: AudioEffectsController,
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val library: StateFlow<LibraryState> = musicRepository.state

    private val _capabilities = MutableStateFlow<EqualizerCapabilities?>(null)
    val capabilities: StateFlow<EqualizerCapabilities?> = _capabilities.asStateFlow()

    private val _capabilitiesLoaded = MutableStateFlow(false)
    val capabilitiesLoaded: StateFlow<Boolean> = _capabilitiesLoaded.asStateFlow()

    private val _rescan = MutableStateFlow<RescanState>(RescanState.Idle)
    val rescan: StateFlow<RescanState> = _rescan.asStateFlow()

    init {
        viewModelScope.launch {
            _capabilities.value = withContext(Dispatchers.Default) { effects.capabilities }
            _capabilitiesLoaded.value = true
        }
    }

    fun setThemeMode(mode: ThemeMode) = launch { settingsRepository.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = launch { settingsRepository.setDynamicColor(enabled) }
    fun setVolume(volume: Float) = launch { settingsRepository.setVolume(volume) }
    fun setEqualizerEnabled(enabled: Boolean) = launch { settingsRepository.setEqualizerEnabled(enabled) }
    fun selectPreset(preset: EqualizerCapabilities.Preset) =
        launch { settingsRepository.setEqualizerPreset(preset.index, preset.bandLevels) }
    fun setBandLevels(levels: List<Int>) = launch { settingsRepository.setBandLevels(levels) }
    fun setBassBoost(strength: Int) = launch { settingsRepository.setBassBoost(strength) }
    fun setVirtualizer(strength: Int) = launch { settingsRepository.setVirtualizer(strength) }
    fun resetSound() = launch { settingsRepository.resetSound() }

    fun rescanLibrary() {
        if (_rescan.value == RescanState.Running) return
        _rescan.value = RescanState.Running
        viewModelScope.launch { _rescan.value = RescanState.Done(musicRepository.rescan()) }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
