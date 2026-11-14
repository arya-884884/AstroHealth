package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpaceDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF04201B),
    primaryContainer = Color(0xFF00382E),
    onPrimaryContainer = Color(0xFF70FDE1),

    secondary = ElectricBlue,
    onSecondary = Color(0xFF00274D),
    secondaryContainer = Color(0xFF0A3B6B),
    onSecondaryContainer = Color(0xFFCCE8FF),

    tertiary = SpacePurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3B1261),
    onTertiaryContainer = Color(0xFFE9D5FF),

    background = DeepSpaceBackground,
    onBackground = StarWhite,

    surface = DeepSpaceSurface,
    onSurface = StarWhite,

    surfaceVariant = DeepSpaceSurfaceVariant,
    onSurfaceVariant = SlateGray,

    outline = Color(0xFF2C3E6B),
    outlineVariant = Color(0xFF1E2B4A),

    error = StatusCriticalRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve deep space telemetry theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SpaceDarkColorScheme,
        typography = Typography,
        content = content
    )
}
