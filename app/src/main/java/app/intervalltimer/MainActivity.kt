package app.intervalltimer

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.intervalltimer.ui.AppTheme
import app.intervalltimer.ui.EditorScreen
import app.intervalltimer.ui.HomeScreen
import app.intervalltimer.ui.SettingsScreen
import app.intervalltimer.ui.TimerScreen

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Lautstärketasten steuern die Medien-Lautstärke (auf der der Gong läuft)
        volumeControlStream = AudioManager.STREAM_MUSIC

        setContent {
            AppTheme {
                val screen by vm.screen.collectAsStateWithLifecycle()
                when (val s = screen) {
                    Screen.Home -> HomeScreen(vm)
                    is Screen.Editor -> EditorScreen(vm, s.presetId)
                    Screen.Timer -> TimerScreen(vm)
                    Screen.Settings -> SettingsScreen(vm)
                }
            }
        }
    }
}
