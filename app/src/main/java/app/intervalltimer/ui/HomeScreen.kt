package app.intervalltimer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
fun HomeScreen(vm: AppViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val quick by vm.quick.collectAsStateWithLifecycle()
    val presets by vm.presets.collectAsStateWithLifecycle()

    var expanded by rememberSaveable { mutableStateOf(true) }
    var menuOpen by remember { mutableStateOf(false) }
    var saveDialog by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Preset?>(null) }

    Column(Modifier.fillMaxSize().background(AppColors.Background)) {
        // ---------- Obere Leiste: Lautstärke + Menü ----------
        Box(Modifier.fillMaxWidth().background(AppColors.TopBar).statusBarsPadding()) {
            VolumeBar(
                volume = settings.volume,
                onChange = vm::setVolume,
                onRelease = vm::previewGong,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                trailing = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, "Menü", tint = Color.White)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Einstellungen") },
                                onClick = { menuOpen = false; vm.navigate(Screen.Settings) },
                            )
                        }
                    }
                },
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(10.dp),
        ) {
            // ---------- Schnellstart-Karte ----------
            Column(Modifier.fillMaxWidth().background(AppColors.Card)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 22.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Schnellstart", color = AppColors.Text, fontSize = 30.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    Icon(
                        if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Einklappen" else "Ausklappen",
                        tint = AppColors.Text, modifier = Modifier.size(32.dp),
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Column {
                        NumberStepper("Sets", quick.sets, 1..99) { vm.updateQuick(quick.copy(sets = it)) }
                        TimeStepper("Training", quick.work, 1) { vm.updateQuick(quick.copy(work = it)) }
                        TimeStepper("Pause", quick.rest, 0) { vm.updateQuick(quick.copy(rest = it)) }
                        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { saveDialog = true }) {
                                Icon(Icons.Filled.Save, null, tint = AppColors.Text, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("SPEICHERN", color = AppColors.Text, fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                            }
                        }
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(AppColors.CardAction)
                        .clickable { vm.startTimer(quick.toPreset()) }
                        .padding(vertical = 26.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Bolt, null, tint = AppColors.Accent, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("START", color = AppColors.Text, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp)
                }
            }

            Spacer(Modifier.height(36.dp))

            // ---------- Voreinstellungen ----------
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (presets.isEmpty()) "NOCH KEINE VOREINSTELLUNGEN" else "VOREINSTELLUNGEN",
                    color = AppColors.Text, fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp, modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { vm.navigate(Screen.Editor(null)) },
                    border = BorderStroke(2.dp, AppColors.Text),
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(Icons.Filled.Add, null, tint = AppColors.Text)
                    Spacer(Modifier.width(8.dp))
                    Text("HINZUFÜGEN", color = AppColors.Text, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
            }
            Spacer(Modifier.height(16.dp))

            if (presets.isEmpty()) {
                Text(
                    "Verwenden Sie die Schaltfläche SPEICHERN, um Ihre erste Voreinstellung schnell zu " +
                        "erstellen und zu speichern.\nFür erweiterte Einstellungen verwenden Sie die " +
                        "Schaltfläche HINZUFÜGEN.",
                    color = AppColors.Text, fontSize = 17.sp, lineHeight = 26.sp,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            } else {
                presets.forEach { p ->
                    PresetCard(
                        preset = p,
                        skipLastRest = settings.skipLastRest,
                        onStart = { vm.startTimer(p) },
                        onEdit = { vm.navigate(Screen.Editor(p.id)) },
                        onDelete = { toDelete = p },
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (saveDialog) {
        NameDialog(
            initial = vm.nextPresetName(),
            onDismiss = { saveDialog = false },
            onConfirm = { name ->
                vm.savePreset(quick.toPreset(name).copy(id = System.currentTimeMillis()))
                saveDialog = false
            },
        )
    }

    toDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Löschen?") },
            text = { Text("„${p.name}“ wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = { vm.deletePreset(p.id); toDelete = null }) { Text("Löschen") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Abbrechen") } },
        )
    }
}

@Composable
private fun PresetCard(
    preset: Preset,
    skipLastRest: Boolean,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(AppColors.Card)
            .clickable(onClick = onStart)
            .padding(start = 18.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.PlayArrow, "Starten", tint = AppColors.Accent, modifier = Modifier.size(34.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(preset.name, color = AppColors.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "${preset.sets} × ${formatTime(preset.work)} / ${formatTime(preset.rest)}  ·  " +
                    "Total ${formatTime(preset.totalSeconds(skipLastRest))}",
                color = AppColors.TextDim, fontSize = 15.sp,
            )
        }
        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Bearbeiten", tint = AppColors.TextDim) }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Löschen", tint = AppColors.TextDim) }
    }
}

@Composable
private fun NameDialog(initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Voreinstellung speichern") },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true,
                label = { Text("Name") })
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim().ifEmpty { initial }) }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}
