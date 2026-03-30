package com.forgelegends.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = ElectricBlue,
    tertiary = HoloPurple,
    background = VoidBlack,
    surface = DeepSpace,
    surfaceVariant = NebulaSurface,
    onPrimary = VoidBlack,
    onSecondary = VoidBlack,
    onTertiary = CoolWhite,
    onBackground = CoolWhite,
    onSurface = CoolWhite,
    onSurfaceVariant = GhostText,
    error = NeonMagenta,
    onError = VoidBlack
)

@Composable
fun ForgeLegendTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
