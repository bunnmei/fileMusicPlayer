package space.webkombinat.musicplayer.playback

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import space.webkombinat.musicplayer.data.EqualizerSettings
import javax.inject.Inject
import javax.inject.Singleton

/** 端末のイコライザーが対応している内容。設定画面の表示に使う */
data class EqualizerCapabilities(
    /** 各帯域の中心周波数（Hz） */
    val bandFrequencies: List<Int>,
    /** 帯域レベルの範囲（ミリベル） */
    val minLevel: Int,
    val maxLevel: Int,
    val presets: List<Preset>,
    val bassBoostSupported: Boolean,
    val virtualizerSupported: Boolean,
) {
    data class Preset(val index: Int, val name: String, val bandLevels: List<Int>)
}

/**
 * Android 標準の audiofx（イコライザー・低音ブースト・バーチャライザー）をプレイヤーに掛ける。
 * 端末によっては一部または全部が使えないので、失敗しても再生は止めない。
 */
@Singleton
class AudioEffectsController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var lastSettings = EqualizerSettings()

    /** 端末が対応している内容。イコライザーが使えない端末では null */
    val capabilities: EqualizerCapabilities? by lazy { queryCapabilities() }

    fun attach(audioSessionId: Int) {
        release()
        equalizer = create("Equalizer") { Equalizer(0, audioSessionId) }
        bassBoost = create("BassBoost") { BassBoost(0, audioSessionId) }
        virtualizer = create("Virtualizer") { Virtualizer(0, audioSessionId) }
        apply(lastSettings)
    }

    fun apply(settings: EqualizerSettings) {
        lastSettings = settings
        equalizer?.safely {
            enabled = settings.enabled
            if (settings.enabled) {
                for (band in 0 until numberOfBands) {
                    setBandLevel(band.toShort(), (settings.bandLevels.getOrNull(band) ?: 0).toShort())
                }
            }
        }
        bassBoost?.safely {
            val on = settings.enabled && settings.bassBoost > 0 && strengthSupported
            enabled = on
            if (on) setStrength(settings.bassBoost.toShort())
        }
        virtualizer?.safely {
            val on = settings.enabled && settings.virtualizer > 0 && strengthSupported
            enabled = on
            if (on) setStrength(settings.virtualizer.toShort())
        }
    }

    fun release() {
        listOfNotNull(equalizer, bassBoost, virtualizer).forEach { it.safely { release() } }
        equalizer = null
        bassBoost = null
        virtualizer = null
    }

    private fun queryCapabilities(): EqualizerCapabilities? {
        val session = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).generateAudioSessionId()
        val eq = create("Equalizer") { Equalizer(0, session) } ?: return null
        return try {
            val range = eq.bandLevelRange
            val bands = eq.numberOfBands.toInt()
            val presets = (0 until eq.numberOfPresets).map { index ->
                eq.usePreset(index.toShort())
                EqualizerCapabilities.Preset(
                    index = index,
                    name = eq.getPresetName(index.toShort()),
                    bandLevels = (0 until bands).map { eq.getBandLevel(it.toShort()).toInt() },
                )
            }
            EqualizerCapabilities(
                bandFrequencies = (0 until bands).map { eq.getCenterFreq(it.toShort()) / 1000 },
                minLevel = range[0].toInt(),
                maxLevel = range[1].toInt(),
                presets = presets,
                bassBoostSupported = create("BassBoost") { BassBoost(0, session) }
                    ?.let { it.strengthSupported.also { _ -> it.release() } } ?: false,
                virtualizerSupported = create("Virtualizer") { Virtualizer(0, session) }
                    ?.let { it.strengthSupported.also { _ -> it.release() } } ?: false,
            )
        } catch (e: RuntimeException) {
            Log.w(TAG, "Failed to query equalizer", e)
            null
        } finally {
            eq.release()
        }
    }

    private inline fun <T : AudioEffect> create(name: String, block: () -> T): T? = try {
        block()
    } catch (e: Exception) {
        Log.w(TAG, "$name is not supported on this device", e)
        null
    }

    private inline fun <T : AudioEffect> T.safely(block: T.() -> Unit) {
        try {
            block()
        } catch (e: RuntimeException) {
            Log.w(TAG, "Audio effect operation failed", e)
        }
    }

    private companion object {
        const val TAG = "AudioEffects"
    }
}
