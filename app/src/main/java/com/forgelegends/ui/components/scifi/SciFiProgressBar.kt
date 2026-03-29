package com.forgelegends.ui.components.scifi

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.forgelegends.ui.theme.CyanGlow
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.NebulaSurface
import com.forgelegends.ui.theme.NeonCyan

@Composable
fun SciFiProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    showScanline: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val scanlineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanlineOffset"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
    ) {
        val barHeight = size.height
        val cornerRadius = CornerRadius(barHeight / 2f)

        // Track
        drawRoundRect(
            color = NebulaSurface,
            cornerRadius = cornerRadius,
            size = Size(size.width, barHeight)
        )

        // Fill
        val fillWidth = size.width * progress.coerceIn(0f, 1f)
        if (fillWidth > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(NeonCyan, ElectricBlue),
                    endX = fillWidth
                ),
                cornerRadius = cornerRadius,
                size = Size(fillWidth, barHeight)
            )

            // Glow under fill
            drawRoundRect(
                color = CyanGlow,
                cornerRadius = cornerRadius,
                topLeft = Offset(0f, barHeight * 0.25f),
                size = Size(fillWidth, barHeight * 0.5f)
            )

            // Scanline highlight
            if (showScanline && fillWidth > 4.dp.toPx()) {
                val scanWidth = 30.dp.toPx()
                val scanX = fillWidth * scanlineOffset
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent),
                        startX = scanX - scanWidth / 2,
                        endX = scanX + scanWidth / 2
                    ),
                    cornerRadius = cornerRadius,
                    size = Size(fillWidth, barHeight)
                )
            }
        }
    }
}
