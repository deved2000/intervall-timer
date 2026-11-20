package app.intervalltimer

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.intervalltimer.audio.GongPlayer
import app.intervalltimer.data.Preset
import app.intervalltimer.data.QuickStart
import app.intervalltimer.data.Settings
import app.intervalltimer.data.Storage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Home : Screen
    data class Editor(val presetId: Long?) : Screen
    data object Timer : Screen
    data object Settings : Screen
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val storage = Storage(app)
    private val gong = GongPlayer(app)

    private val _screen = MutableStateFlow<Screen>(Screen.Home)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _settings = MutableStateFlow(storage.loadSettings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private val _presets = MutableStateFlow(storage.loadPresets())
    val presets: StateFlow<List<Preset>> = _presets.asStateFlow()

    private val _quick = MutableStateFlow(storage.loadQuickStart())
    val quick: StateFlow<QuickStart> = _quick.asStateFlow()

    private val _timer = MutableStateFlow<TimerState?>(null)
    val timer: StateFlow<TimerState?> = _timer.asStateFlow()

    private var engine: TimerEngine? = null
    private var tickJob: Job? = null

    // ---------- Navigation ----------
    fun navigate(s: Screen) {
        _screen.value = s
    }

    // ---------- Einstellungen ----------
    fun updateSettings(s: Settings) {
        _settings.value = s
        storage.saveSettings(s)
    }

    fun setVolume(v: Float) = updateSettings(_settings.value.copy(volume = v.coerceIn(0f, 1f)))

    fun previewGong() = gong.play(_settings.value.volume)

    // ---------- Schnellstart ----------
    fun updateQuick(q: QuickStart) {
        _quick.value = q
        storage.saveQuickStart(q)
    }

    // ---------- Voreinstellungen ----------
    fun savePreset(p: Preset) {
        val list = _presets.value.toMutableList()
        val i = list.indexOfFirst { it.id == p.id }
        if (i >= 0) list[i] = p else list += p
        _presets.value = list
        storage.savePresets(list)
    }

    fun deletePreset(id: Long) {
        val list = _presets.value.filterNot { it.id == id }
        _presets.value = list
        storage.savePresets(list)
    }

    fun nextPresetName(): String {
        var n = _presets.value.size + 1
        val names = _presets.value.map { it.name }.toSet()
        while ("Voreinstellung $n" in names) n++
        return "Voreinstellung $n"
    }

    // ---------- Timer ----------
    fun startTimer(p: Preset) {
        val s = _settings.value
        val e = TimerEngine(s.finalCount) { gong.play(_settings.value.volume) }
        engine = e
        _timer.value = e.start(buildSegments(p, s.skipLastRest))
        _screen.value = Screen.Timer
        runTicker()
    }

    private fun runTicker() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            var last = SystemClock.elapsedRealtime()
            while (isActive) {
                delay(TICK_MS)
                val now = SystemClock.elapsedRealtime()
                val dt = now - last
                last = now
                val st = _timer.value ?: break
                val e = engine ?: break
                if (st.finished) break
                _timer.value = e.advance(st, dt)
            }
        }
    }

    fun togglePause() {
        val st = _timer.value ?: return
        _timer.value = st.copy(paused = !st.paused)
    }

    fun skipNext() {
        val st = _timer.value ?: return
        val e = engine ?: return
        _timer.value = e.next(st)
    }

    fun skipPrevious() {
        val st = _timer.value ?: return
        val e = engine ?: return
        _timer.value = e.previous(st)
    }

    fun stopTimer() {
        tickJob?.cancel()
        tickJob = null
        engine = null
        _timer.value = null
        _screen.value = Screen.Home
    }

    override fun onCleared() {
        tickJob?.cancel()
        gong.release()
    }

    companion object {
        private const val TICK_MS = 25L
    }
}
