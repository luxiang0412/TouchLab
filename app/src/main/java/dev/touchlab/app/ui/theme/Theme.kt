package dev.touchlab.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TouchLabColors = darkColorScheme(
    primary = JellyMint,
    onPrimary = Background,
    secondary = DustLavender,
    onSecondary = Background,
    tertiary = MarbleCoral,
    onTertiary = Background,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSoft,
    outlineVariant = SurfaceRaised,
    error = MarbleCoral,
)

@Composable
fun TouchLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TouchLabColors,
        typography = TouchLabTypography,
        content = content,
    )
}
