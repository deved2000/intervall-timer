package app.intervalltimer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Plus/Minus-Knopf: Tippen = ein Schritt, Gedrückthalten = schnell wiederholen.
 * [onStep] bekommt die Anzahl der bisherigen Wiederholungen (für Beschleunigung).
 */
@Composable
fun RepeatIconButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onStep: (repeat: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val step by rememberUpdatedState(onStep)
    val isEnabled by rememberUpdatedState(enabled)
    Box(
        modifier = Modifier
            .size(64.dp)
            .semantics {
                role = Role.Button
                contentDescription = description
                onClick { if (isEnabled) step(0); true }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    if (!isEnabled) {
                        waitForUpOrCancellation()
                        return@awaitEachGesture
                    }
                    step(0)
                    val job = scope.launch {
                        delay(400)
                        var n = 1
                        var wait = 130L
                        while (isEnabled) {
                            step(n++)
                            delay(wait)
                            wait = max(35L, wait - 10L)
                        }
                    }
                    waitForUpOrCancellation()
                    job.cancel()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) AppColors.Stepper else AppColors.Stepper.copy(alpha = 0.3f),
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun StepperRow(
    label: String,
    onMinus: (Int) -> Unit,
    onPlus: (Int) -> Unit,
    minusEnabled: Boolean,
    plusEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label.uppercase(), color = AppColors.Text, fontSize = 19.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RepeatIconButton(Icons.Filled.IndeterminateCheckBox, "$label verringern", minusEnabled, onMinus)
            Box(Modifier.widthIn(min = 160.dp), contentAlignment = Alignment.Center) { content() }
            RepeatIconButton(Icons.Filled.AddBox, "$label erhöhen", plusEnabled, onPlus)
        }
    }
}

@Composable
fun NumberStepper(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    StepperRow(
        label = label,
        onMinus = { onChange((value - 1).coerceIn(range)) },
        onPlus = { onChange((value + 1).coerceIn(range)) },
        minusEnabled = value > range.first,
        plusEnabled = value < range.last,
    ) {
        Text(value.toString(), color = AppColors.Text, fontSize = 46.sp, fontWeight = FontWeight.Bold)
    }
}

/** Zeit als "MM : SS". Tippen auf die Zahl öffnet eine direkte Eingabe. */
@Composable
fun TimeStepper(label: String, seconds: Int, min: Int, onChange: (Int) -> Unit) {
    val maxSec = 99 * 60 + 59
    var editing by remember { mutableStateOf(false) }
    fun stepSize(repeat: Int) = if (repeat > 25) 5 else 1
    StepperRow(
        label = label,
        onMinus = { r -> onChange((seconds - stepSize(r)).coerceIn(min, maxSec)) },
        onPlus = { r -> onChange((seconds + stepSize(r)).coerceIn(min, maxSec)) },
        minusEnabled = seconds > min,
        plusEnabled = seconds < maxSec,
    ) {
        Row(
            modifier = Modifier.clickable { editing = true }.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val style = Modifier.width(72.dp)
            Text("%02d".format(seconds / 60), style, color = AppColors.Text, fontSize = 46.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(":", color = AppColors.Text, fontSize = 46.sp, fontWeight = FontWeight.Bold)
            Text("%02d".format(seconds % 60), style, color = AppColors.Text, fontSize = 46.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
    if (editing) {
        TimeInputDialog(
            title = label,
            seconds = seconds,
            onDismiss = { editing = false },
            onConfirm = { onChange(it.coerceIn(min, maxSec)); editing = false },
        )
    }
}

@Composable
private fun TimeInputDialog(title: String, seconds: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var m by remember { mutableStateOf((seconds / 60).toString()) }
    var s by remember { mutableStateOf((seconds % 60).toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                OutlinedTextField(
                    value = m, onValueChange = { v -> m = v.filter(Char::isDigit).take(2) },
                    label = { Text("Min") }, singleLine = true, modifier = Modifier.width(96.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Text("  :  ", fontSize = 24.sp)
                OutlinedTextField(
                    value = s, onValueChange = { v -> s = v.filter(Char::isDigit).take(2) },
                    label = { Text("Sek") }, singleLine = true, modifier = Modifier.width(96.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val total = (m.toIntOrNull() ?: 0) * 60 + (s.toIntOrNull() ?: 0).coerceAtMost(59)
                onConfirm(total)
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}

/** Lautsprecher-Symbol + Schieberegler. Beim Loslassen ertönt ein Probe-Gong. */
@Composable
fun VolumeBar(
    volume: Float,
    onChange: (Float) -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (volume <= 0f) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Lautstärke",
            tint = Color.White,
            modifier = Modifier.size(36.dp),
        )
        Spacer(Modifier.width(16.dp))
        Slider(
            value = volume,
            onValueChange = onChange,
            onValueChangeFinished = onRelease,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
            ),
        )
        trailing()
    }
}
