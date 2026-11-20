package app.intervalltimer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.intervalltimer.AppViewModel
import app.intervalltimer.Screen
import app.intervalltimer.data.Phase
import app.intervalltimer.data.Settings

private val Palette = listOf(
    0xFFC75000, 0xFFE65100, 0xFFD84315, 0xFFC62828, 0xFFAD1457, 0xFF6A1B9A,
    0xFF4527A0, 0xFF283593, 0xFF1565C0, 0xFF1B7CC0, 0xFF0277BD, 0xFF00838F,
    0xFF008792, 0xFF00695C, 0xFF00883F, 0xFF2E7D32, 0xFF558B2F, 0xFF9E9D24,
    0xFFF9A825, 0xFF4E342E, 0xFF37474F, 0xFF455A64, 0xFF212121, 0xFF5D4037,
)

private fun Phase.title() = when (this) {
    Phase.PREPARE -> "Vorbereiten"
    Phase.WORK -> "Training"
    Phase.REST -> "Pause"
    Phase.COOLDOWN -> "Abkühlen"
    Phase.FINISH -> "Fertig"
}

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val s by vm.settings.collectAsStateWithLifecycle()
    var pickFor by remember { mutableStateOf<Phase?>(null) }
    BackHandler { vm.navigate(Screen.Home) }

    Column(Modifier.fillMaxSize().background(AppColors.Background)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { vm.navigate(Screen.Home) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück", tint = AppColors.Text)
            }
            Text("EINSTELLUNGEN", color = AppColors.Text, fontSize = 28.sp)
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            SectionTitle("Sound")
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    RowLabel("Final Count")
                    Text(
                        finalCountHint(s.finalCount),
                        color = AppColors.TextDim, fontSize = 14.sp,
                    )
                }
                RepeatIconButton(Icons.Filled.IndeterminateCheckBox, "Final Count verringern", s.finalCount > 0) {
                    vm.updateSettings(s.copy(finalCount = (s.finalCount - 1).coerceAtLeast(0)))
                }
                Text(
                    s.finalCount.toString(), color = AppColors.Text, fontSize = 40.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                RepeatIconButton(Icons.Filled.AddBox, "Final Count erhöhen", s.finalCount < Settings.MAX_FINAL_COUNT) {
                    vm.updateSettings(s.copy(finalCount = (s.finalCount + 1).coerceAtMost(Settings.MAX_FINAL_COUNT)))
                }
            }

            SectionTitle("Farben")
            Phase.entries.forEach { phase ->
                Row(
                    Modifier.fillMaxWidth().clickable { pickFor = phase }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RowLabel(phase.title(), Modifier.weight(1f))
                    ColorDot(Color(s.colorOf(phase)), 56)
                }
            }

            SectionTitle("Sonstiges")
            Row(
                Modifier.fillMaxWidth().clickable { vm.updateSettings(s.copy(skipLastRest = !s.skipLastRest)) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RowLabel("Letzte Pause überspringen", Modifier.weight(1f))
                Switch(
                    checked = s.skipLastRest,
                    onCheckedChange = { vm.updateSettings(s.copy(skipLastRest = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.Accent,
                        checkedTrackColor = Color(0xFF8C8A55),
                        checkedBorderColor = Color.Transparent,
                    ),
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    pickFor?.let { phase ->
        AlertDialog(
            onDismissRequest = { pickFor = null },
            title = { Text(phase.title()) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Palette.chunked(6).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { c ->
                                val selected = c == s.colorOf(phase)
                                Box(
                                    Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(c))
                                        .then(if (selected) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
                                        .clickable {
                                            vm.updateSettings(s.withColor(phase, c))
                                            pickFor = null
                                        },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pickFor = null }) { Text("Schließen") } },
            dismissButton = {
                TextButton(onClick = {
                    vm.updateSettings(s.withColor(phase, Settings().colorOf(phase)))
                    pickFor = null
                }) { Text("Standard") }
            },
        )
    }
}

private fun finalCountHint(n: Int): String = when (n) {
    0 -> "Gong nur beim Ende von Training/Pause"
    1 -> "Gong bei 1 s vor Ende und beim Ende"
    else -> "Gong ab $n s vor Ende jede Sekunde bis zum Ende"
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text, color = AppColors.Text.copy(alpha = 0.85f), fontSize = 30.sp,
        modifier = Modifier.padding(top = 28.dp, bottom = 8.dp),
    )
}

@Composable
private fun RowLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, color = AppColors.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = modifier)
}

/** Farbkreis wie im Design: Hauptfarbe mit hellerem Diagonal-Segment. */
@Composable
private fun ColorDot(color: Color, sizeDp: Int) {
    val light = lerp(color, Color.White, 0.25f)
    Canvas(Modifier.size(sizeDp.dp).clip(CircleShape)) {
        drawRect(color)
        val path = Path().apply {
            moveTo(size.width * 0.45f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * 0.85f)
            close()
        }
        drawPath(path, light)
    }
}
