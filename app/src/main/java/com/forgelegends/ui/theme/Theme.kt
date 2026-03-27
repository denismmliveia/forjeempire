package com.forgelegends.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ForgeOrange,
    secondary = ForgeGold,
    tertiary = SteelBlue,
    background = DarkForge,
    surface = DarkSurface,
    surfaceVariant = MediumSurface,
    onPrimary = DarkForge,
    onSecondary = DarkForge,
    onBackground = LightText,
    onSurface = LightText,
    onSurfaceVariant = MutedText,
    error = EmberRed
)

@Composable
fun ForgeLegendTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
