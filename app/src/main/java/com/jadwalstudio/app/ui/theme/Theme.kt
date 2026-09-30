package com.jadwalstudio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SageGreen,
    onPrimary = White,
    background = BgCream,
    onBackground = TextDark,
    surface = BgCream,
    onSurface = TextDark,
    secondary = SageGreenDark,
    onSecondary = White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFDFE7E1),
    onPrimaryContainer = SageGreenDark,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF0ECE5),
    onSurfaceVariant = TextDescription,
    outline = TextMuted,
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFDFD9CE)
)

@Composable
fun JadwalStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
