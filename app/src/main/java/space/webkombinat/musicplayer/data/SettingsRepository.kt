package space.webkombinat.musicplayer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class EqualizerSettings(
    val enabled: Boolean = false,
    /** 端末のプリセット番号。-1 はカスタム */
    val preset: Int = CUSTOM_PRESET,
    /** 帯域ごとのレベル（ミリベル）。空なら 0dB */
    val bandLevels: List<Int> = emptyList(),
    /** 低音ブーストの強さ 0〜1000 */
    val bassBoost: Int = 0,
    /** バーチャライザー（立体音響）の強さ 0〜1000 */
    val virtualizer: Int = 0,
) {
    companion object {
        const val CUSTOM_PRESET = -1
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    /** アプリ内音量 0.0〜1.0（端末の音量とは別） */
    val volume: Float = 1f,
    val equalizer: EqualizerSettings = EqualizerSettings(),
    /** ライブラリに表示しないフォルダ（末尾 "/" のパス。配下のフォルダも除外される） */
    val excludedFolders: Set<String> = emptySet(),
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.dataStore

    val settings: Flow<AppSettings> = dataStore.data.map { p ->
        AppSettings(
            themeMode = p[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            dynamicColor = p[DYNAMIC_COLOR] ?: true,
            volume = p[VOLUME] ?: 1f,
            equalizer = EqualizerSettings(
                enabled = p[EQ_ENABLED] ?: false,
                preset = p[EQ_PRESET] ?: EqualizerSettings.CUSTOM_PRESET,
                bandLevels = p[EQ_BAND_LEVELS]?.split(',')?.mapNotNull { it.toIntOrNull() }.orEmpty(),
                bassBoost = p[BASS_BOOST] ?: 0,
                virtualizer = p[VIRTUALIZER] ?: 0,
            ),
            excludedFolders = p[EXCLUDED_FOLDERS].orEmpty(),
        )
    }.distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) = dataStore.edit { it[THEME_MODE] = mode.name }

    suspend fun setDynamicColor(enabled: Boolean) = dataStore.edit { it[DYNAMIC_COLOR] = enabled }

    suspend fun setVolume(volume: Float) = dataStore.edit { it[VOLUME] = volume.coerceIn(0f, 1f) }

    suspend fun setEqualizerEnabled(enabled: Boolean) = dataStore.edit { it[EQ_ENABLED] = enabled }

    /** プリセットを選んだときは、そのプリセットの帯域レベルも一緒に保存する */
    suspend fun setEqualizerPreset(preset: Int, bandLevels: List<Int>) = dataStore.edit {
        it[EQ_PRESET] = preset
        it[EQ_BAND_LEVELS] = bandLevels.joinToString(",")
    }

    suspend fun setBandLevels(bandLevels: List<Int>) = dataStore.edit {
        it[EQ_PRESET] = EqualizerSettings.CUSTOM_PRESET
        it[EQ_BAND_LEVELS] = bandLevels.joinToString(",")
    }

    suspend fun setBassBoost(strength: Int) = dataStore.edit { it[BASS_BOOST] = strength.coerceIn(0, 1000) }

    suspend fun setVirtualizer(strength: Int) = dataStore.edit { it[VIRTUALIZER] = strength.coerceIn(0, 1000) }

    /** 除外するときは、すでに除外していた配下のフォルダを整理してから追加する */
    suspend fun setFolderExcluded(path: String, excluded: Boolean) = dataStore.edit {
        val current = it[EXCLUDED_FOLDERS].orEmpty()
        it[EXCLUDED_FOLDERS] = if (excluded) {
            current.filterNot { folder -> folder.startsWith(path) }.toSet() + path
        } else {
            current - path
        }
    }

    suspend fun resetSound() = dataStore.edit {
        it.remove(EQ_PRESET)
        it.remove(EQ_BAND_LEVELS)
        it.remove(BASS_BOOST)
        it.remove(VIRTUALIZER)
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val VOLUME = floatPreferencesKey("volume")
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val EQ_PRESET = intPreferencesKey("eq_preset")
        val EQ_BAND_LEVELS = stringPreferencesKey("eq_band_levels")
        val BASS_BOOST = intPreferencesKey("bass_boost")
        val VIRTUALIZER = intPreferencesKey("virtualizer")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
    }
}
