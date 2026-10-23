package space.webkombinat.musicplayer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import space.webkombinat.musicplayer.data.SettingsRepository
import space.webkombinat.musicplayer.ui.MusicPlayerApp
import space.webkombinat.musicplayer.ui.theme.MusicPlayerTheme
import space.webkombinat.musicplayer.ui.theme.isDark
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 設定を読み込むまでは何も描かない（一瞬だけ違うテーマが見えるのを防ぐ）
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = null)
            val current = settings ?: return@setContent
            val darkTheme = current.themeMode.isDark()

            // ステータスバー・ナビゲーションバーのアイコン色をアプリのテーマに合わせる
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                )
                onDispose {}
            }

            MusicPlayerTheme(darkTheme = darkTheme, dynamicColor = current.dynamicColor) {
                MusicPlayerApp()
            }
        }
    }
}
