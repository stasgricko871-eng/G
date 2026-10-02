package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RetroDarkColorScheme = darkColorScheme(
    primary = RetroPrimary,
    onPrimary = RetroBlack,
    primaryContainer = RetroSurfaceVariant,
    onPrimaryContainer = RetroWhite,
    secondary = RetroSecondary,
    onSecondary = RetroBlack,
    tertiary = RetroAccent,
    background = RetroBlack,
    onBackground = RetroWhite,
    surface = RetroSurface,
    onSurface = RetroWhite,
    surfaceVariant = RetroSurfaceVariant,
    onSurfaceVariant = RetroGray,
    outline = RetroBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Keep consistent high-contrast retro aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RetroDarkColorScheme,
        typography = Typography,
        content = content
    )
}
