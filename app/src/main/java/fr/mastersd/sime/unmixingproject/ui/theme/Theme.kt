package fr.mastersd.sime.unmixingproject.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val UnmixingColorScheme = darkColorScheme(
    primary = Color(0xFF00F5FF),
    onPrimary = Color(0xFF080C14),
    primaryContainer = Color(0xFF00B8C4),
    secondary = Color(0xFF7C3AFF),
    onSecondary = Color(0xFFE8F4FF),
    secondaryContainer = Color(0xFF5B2BD6),
    background = Color(0xFF080C14),
    onBackground = Color(0xFFE8F4FF),
    surface = Color(0xFF0D1421),
    onSurface = Color(0xFFE8F4FF),
    surfaceVariant = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF8BA3C0),
    outline = Color(0xFF1E2D45),
    error = Color(0xFFFF4D6D),
    onError = Color(0xFF080C14)
)

@Composable
fun UnmixingProjectTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UnmixingColorScheme,
        typography = Typography,
        content = content
    )
}