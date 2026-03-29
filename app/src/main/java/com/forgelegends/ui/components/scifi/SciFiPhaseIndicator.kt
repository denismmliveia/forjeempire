package com.forgelegends.ui.components.scifi

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.forgelegends.ui.theme.GhostText
import com.forgelegends.ui.theme.NebulaSurface
import com.forgelegends.ui.theme.NeonCyan
import com.forgelegends.ui.theme.NeonGreen
import kotlin.math.cos
import kotlin.math.sin

enum class PhaseState { Completed, Active, Locked }

@Composable
fun SciFiPhaseIndicator(
    state: PhaseState,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val radius = this.size.minDimension / 2f * 0.85f

        when (state) {
            PhaseState.Completed -> {
                // Filled green hexagon with check
                drawHexagon(cx, cy, radius, NeonGreen, Fill)
                drawCheck(cx, cy, radius * 0.45f)
            }
            PhaseState.Active -> {
                // Pulsing cyan border hexagon
                drawHexagon(cx, cy, radius, NeonCyan.copy(alpha = 0.15f), Fill)
                drawHexagon(cx, cy, radius, NeonCyan.copy(alpha = pulseAlpha), Stroke(width = 2.dp.toPx()))
            }
            PhaseState.Locked -> {
                // Dark hexagon with muted border
                drawHexagon(cx, cy, radius, NebulaSurface, Fill)
                drawHexagon(cx, cy, radius, GhostText.copy(alpha = 0.4f), Stroke(width = 1.dp.toPx()))
            }
        }
    }
}

private fun DrawScope.drawHexagon(
    cx: Float, cy: Float, radius: Float,
    color: Color, style: androidx.compose.ui.graphics.drawscope.DrawStyle
) {
    val path = Path().apply {
        for (i in 0..5) {
            val angle = Math.toRadians((60.0 * i) - 30.0)
            val x = cx + radius * cos(angle).toFloat()
            val y = cy + radius * sin(angle).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(path, color, style = style)
}

private fun DrawScope.drawCheck(cx: Float, cy: Float, arm: Float) {
    val path = Path().apply {
        moveTo(cx - arm * 0.6f, cy)
        lineTo(cx - arm * 0.1f, cy + arm * 0.5f)
        lineTo(cx + arm * 0.7f, cy - arm * 0.5f)
    }
    drawPath(path, Color.Black, style = Stroke(width = 2.dp.toPx()))
}
