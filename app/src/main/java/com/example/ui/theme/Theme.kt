package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3E001C),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = ElectricBlue,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004E58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = CyberPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF38006B),
    onTertiaryContainer = Color(0xFFEEDBFF),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF334155),
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeonPink,
    onPrimary = Color.White,
    secondary = ElectricBlue,
    onSecondary = Color(0xFF00363D),
    background = Color(0xFF0E1118),
    surface = Color(0xFF151922),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // YUNA AI defaults to its signature premium dark cyberpunk styling
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
