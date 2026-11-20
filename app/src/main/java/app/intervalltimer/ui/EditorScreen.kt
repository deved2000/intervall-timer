package app.intervalltimer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.intervalltimer.AppViewModel
import app.intervalltimer.Screen
import app.intervalltimer.data.Preset
import app.intervalltimer.data.formatTime

@Composable
fun EditorScreen(vm: AppViewModel, presetId: Long?) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val existing = remember(presetId) { vm.presets.value.firstOrNull { it.id == presetId } }
    var p by remember(presetId) { mutableStateOf(existing ?: Preset()) }

    fun close() = vm.navigate(Screen.Home)
    fun save() {
        vm.savePreset(p.copy(name = p.name.trim().ifEmpty { existing?.name ?: vm.nextPresetName() }))
        close()
    }
    BackHandler { close() }

    Column(Modifier.fillMaxSize().background(AppColors.Background)) {
        // ---------- Obere Leiste ----------
        Row(
            Modifier.fillMaxWidth().background(AppColors.TopBar).statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = ::close) {
                Icon(Icons.Filled.Close, "Schließen", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    if (existing == null) "NEUE VOREINSTELLUNG" else "BEARBEITEN",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = 1.5.sp,
                    modifier = Modifier.border(2.dp, Color.White).padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
            IconButton(onClick = ::save) {
                Icon(Icons.Filled.Save, "Speichern", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(10.dp),
        ) {
            OutlinedTextField(
                value = p.name,
                onValueChange = { p = p.copy(name = it) },
                placeholder = { Text("Name der Voreinstellung", fontSize = 19.sp) },
                singleLine = true,
                shape = RectangleShape,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 19.sp, color = AppColors.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Text,
                    unfocusedBorderColor = AppColors.TextDim.copy(alpha = 0.5f),
                    cursorColor = AppColors.Text,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
            TimeStepper("Vorbereiten", p.prepare, 0) { p = p.copy(prepare = it) }
            NumberStepper("Sets", p.sets, 1..99) { p = p.copy(sets = it) }
            TimeStepper("Training", p.work, 1) { p = p.copy(work = it) }
            TimeStepper("Pause", p.rest, 0) { p = p.copy(rest = it) }
            TimeStepper("Abkühlen", p.cooldown, 0) { p = p.copy(cooldown = it) }
            Spacer(Modifier.height(16.dp))
        }

        // ---------- Gesamtdauer ----------
        Box(
            Modifier.fillMaxWidth().background(AppColors.TotalBar).navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                "TOTAL ${formatTime(p.totalSeconds(settings.skipLastRest))}",
                color = AppColors.TotalText, fontSize = 30.sp, fontWeight = FontWeight.Bold,
            )
        }
    }
}
