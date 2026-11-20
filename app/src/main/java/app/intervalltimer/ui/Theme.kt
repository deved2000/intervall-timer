package app.intervalltimer.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Background = Color(0xFF131A20)
    val TopBar = Color(0xFF546E7A)
    val Card = Color(0xFF334A57)
    val CardAction = Color(0xFF546E7A)
    val Text = Color(0xFFF2F2F2)
    val TextDim = Color(0xFFB0BEC5)
    val Stepper = Color(0xFFE0E0E0)
    val Accent = Color(0xFFFDD835)
    val ControlDark = Color(0xFF2A3F4A)
    val TotalBar = Color(0xFFE6E6E6)
    val TotalText = Color(0xFF34495A)
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AppColors.Accent,
            onPrimary = Color.Black,
            secondary = AppColors.TopBar,
            background = AppColors.Background,
            surface = AppColors.Card,
            onSurface = AppColors.Text,
            onBackground = AppColors.Text,
            surfaceContainerHigh = AppColors.Card,
        ),
        content = content,
    )
}
