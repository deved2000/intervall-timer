package app.intervalltimer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.intervalltimer.AppViewModel
import app.intervalltimer.data.Phase
import app.intervalltimer.data.formatTime
import kotlinx.coroutines.delay

private fun Phase.label() = when (this) {
    Phase.PREPARE -> "VORBEREITEN"
    Phase.WORK -> "TRAINING"
    Phase.REST -> "PAUSE"
    Phase.COOLDOWN -> "ABKÜHLEN"
    Phase.FINISH -> "FERTIG"
}

@Composable
fun TimerScreen(vm: AppViewModel) {
    val state by vm.timer.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val st = state ?: return

    // Bildschirm bleibt an, solange der Timer läuft
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    var controlsVisible by remember { mutableStateOf(false) }
    var interaction by remember { mutableIntStateOf(0) }
    fun showControls() { controlsVisible = true; interaction++ }

    // Steuerung nach 4 s automatisch ausblenden (außer wenn pausiert)
    LaunchedEffect(controlsVisible, interaction, st.paused) {
        if (controlsVisible && !st.paused) {
            delay(4000)
            controlsVisible = false
        }
    }

    BackHandler {
        if (st.finished) vm.stopTimer() else showControls()
    }

    val bg by animateColorAsState(Color(settings.colorOf(st.current.phase)), tween(300), label = "bg")

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(bg)
            .pointerInput(st.finished) {
                detectTapGestures(
                    onTap = { if (st.finished) vm.stopTimer() else showControls() },
                    onLongPress = { vm.stopTimer() },
                )
            },
    ) {
        val density = LocalDensity.current
        val w = maxWidth
        val label = st.current.phase.label()
        val timeSize = with(density) { (w / 2.6f).toSp() }
        val setsSize = with(density) { (w / 3.9f).toSp() }
        val labelSize = with(density) { (w / maxOf(label.length * 0.66f, 5.2f)).toSp() }
        val textColor = Color(0xFFF2F2F2)

        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Verbleibende Sets
            Text(
                text = st.current.setsLeft?.toString() ?: "",
                color = textColor, fontSize = setsSize, fontWeight = FontWeight.Light,
                maxLines = 1, softWrap = false,
            )
            // Zeit
            Text(
                text = if (st.finished) "" else formatTime(st.displaySeconds),
                color = textColor.copy(alpha = if (st.paused) 0.6f else 1f),
                fontSize = timeSize, fontWeight = FontWeight.Normal,
                maxLines = 1, softWrap = false, textAlign = TextAlign.Center,
            )
            // Phase
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = labelSize, fontWeight = FontWeight.Black,
                maxLines = 1, softWrap = false,
            )
            if (st.finished) {
                Spacer(Modifier.height(24.dp))
                Text("Tippen zum Schließen", color = textColor.copy(alpha = 0.8f), fontSize = 18.sp)
            }
        }

        // ---------- Einblendbare Steuerung ----------
        AnimatedVisibility(
            visible = controlsVisible && !st.finished,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    VolumeBar(
                        volume = settings.volume,
                        onChange = { vm.setVolume(it); showControls() },
                        onRelease = vm::previewGong,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { vm.skipPrevious(); showControls() }, Modifier.size(64.dp)) {
                            Icon(Icons.Filled.SkipPrevious, "Zurück", tint = Color.White, modifier = Modifier.size(44.dp))
                        }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                "Zum Beenden gedrückt halten.",
                                color = Color.White, fontSize = 22.sp, textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .background(AppColors.ControlDark, RoundedCornerShape(50))
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                            )
                        }
                        IconButton(onClick = { vm.skipNext(); showControls() }, Modifier.size(64.dp)) {
                            Icon(Icons.Filled.SkipNext, "Weiter", tint = Color.White, modifier = Modifier.size(44.dp))
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { vm.togglePause(); showControls() },
                    containerColor = AppColors.ControlDark,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).size(72.dp),
                ) {
                    Icon(
                        if (st.paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (st.paused) "Fortsetzen" else "Pausieren",
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
        }
    }
}
