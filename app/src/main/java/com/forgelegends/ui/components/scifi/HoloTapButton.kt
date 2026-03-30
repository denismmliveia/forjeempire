package com.forgelegends.ui.components.scifi

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.NeonCyan
import com.forgelegends.ui.theme.VoidBlack
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HoloTapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tapGlow: Float = 0f,
    tapScale: Float = 1f
) {
    val inf = rememberInfiniteTransition(label = "holo_tap")

    // Slow rotation for the inner tech ring
    val innerRotation by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    // Counter-rotation for outer tick marks
    val outerRotation by inf.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rot"
    )

    // Pulsing glow on the outer ring
    val pulseAlpha by inf.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Scanline sweep angle
    val scanAngle by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )

    val textMeasurer = rememberTextMeasurer()

    val interactionSource = remember { MutableInteractionSource() }

    // Combine pulse with tap glow for dynamic brightness
    val effectiveGlow = (pulseAlpha + tapGlow).coerceAtMost(1f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer(
                scaleX = tapScale,
                scaleY = tapScale
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerRadius = size.minDimension / 2f * 0.92f
            val innerRingRadius = outerRadius * 0.78f
            val coreRadius = outerRadius * 0.68f

            // === 1. Background glow bloom ===
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = effectiveGlow * 0.12f),
                        ElectricBlue.copy(alpha = effectiveGlow * 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = outerRadius * 1.5f
                )
            )

            // === 2. Circuit lines radiating outward ===
            drawCircuitLines(cx, cy, outerRadius, effectiveGlow)

            // === 3. Outer glow ring ===
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        NeonCyan.copy(alpha = effectiveGlow * 0.25f),
                        NeonCyan.copy(alpha = effectiveGlow * 0.6f),
                        NeonCyan.copy(alpha = effectiveGlow * 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = outerRadius * 1.12f
                ),
                radius = outerRadius * 1.12f
            )

            // Outer ring stroke
            drawCircle(
                color = NeonCyan.copy(alpha = effectiveGlow * 0.9f),
                radius = outerRadius,
                style = Stroke(width = 3.5f),
                center = Offset(cx, cy)
            )

            // === 4. Outer tick marks (slowly counter-rotating) ===
            rotate(outerRotation, pivot = Offset(cx, cy)) {
                drawOuterTicks(cx, cy, outerRadius, innerRingRadius, effectiveGlow)
            }

            // === 5. Inner tech ring with segmented arcs ===
            rotate(innerRotation, pivot = Offset(cx, cy)) {
                drawInnerTechRing(cx, cy, innerRingRadius, effectiveGlow)
            }

            // === 6. Scanline sweep ===
            drawScanline(cx, cy, innerRingRadius, coreRadius, scanAngle, effectiveGlow)

            // === 7. Dark core fill ===
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VoidBlack.copy(alpha = 0.95f),
                        Color(0xFF0D1520).copy(alpha = 0.9f),
                        Color(0xFF0A1018)
                    ),
                    center = Offset(cx, cy),
                    radius = coreRadius
                ),
                radius = coreRadius
            )

            // Core edge ring
            drawCircle(
                color = NeonCyan.copy(alpha = effectiveGlow * 0.35f),
                radius = coreRadius,
                style = Stroke(width = 1.5f),
                center = Offset(cx, cy)
            )

            // === 8. Tap flash overlay ===
            if (tapGlow > 0.01f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = tapGlow * 0.15f),
                            NeonCyan.copy(alpha = tapGlow * 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = coreRadius
                    ),
                    radius = coreRadius
                )
            }

            // === 9. "TAP" text ===
            val tapStyle = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                letterSpacing = 4.sp
            )
            val tapLayout = textMeasurer.measure("TAP", tapStyle)
            drawText(
                textLayoutResult = tapLayout,
                topLeft = Offset(
                    cx - tapLayout.size.width / 2f,
                    cy - tapLayout.size.height / 2f - 6.dp.toPx()
                )
            )

            // === 10. Chevron below text ===
            drawChevron(cx, cy + coreRadius * 0.35f, effectiveGlow)
        }
    }
}

private fun DrawScope.drawCircuitLines(
    cx: Float, cy: Float, outerRadius: Float, alpha: Float
) {
    val lineColor = NeonCyan.copy(alpha = alpha * 0.15f)
    val nodeColor = NeonCyan.copy(alpha = alpha * 0.3f)

    // 8 circuit lines radiating from the ring
    val angles = listOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f)
    for (angle in angles) {
        val rad = angle * PI.toFloat() / 180f
        val startR = outerRadius * 1.05f
        val endR = outerRadius * 1.35f
        val sx = cx + cos(rad) * startR
        val sy = cy + sin(rad) * startR
        val ex = cx + cos(rad) * endR
        val ey = cy + sin(rad) * endR

        drawLine(
            color = lineColor,
            start = Offset(sx, sy),
            end = Offset(ex, ey),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )

        // Small node dot at the end
        drawCircle(
            color = nodeColor,
            radius = 2.5f,
            center = Offset(ex, ey)
        )

        // Short perpendicular branches on some lines
        if (angle.toInt() % 90 == 0) {
            val midR = outerRadius * 1.2f
            val mx = cx + cos(rad) * midR
            val my = cy + sin(rad) * midR
            val perpRad = rad + PI.toFloat() / 2f
            val bLen = outerRadius * 0.08f
            drawLine(
                color = lineColor,
                start = Offset(mx, my),
                end = Offset(mx + cos(perpRad) * bLen, my + sin(perpRad) * bLen),
                strokeWidth = 1f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawOuterTicks(
    cx: Float, cy: Float, outerR: Float, innerR: Float, alpha: Float
) {
    val tickColor = NeonCyan.copy(alpha = alpha * 0.5f)
    val tickColorBright = NeonCyan.copy(alpha = alpha * 0.8f)
    val totalTicks = 60
    val gapRadius = (outerR + innerR) / 2f

    for (i in 0 until totalTicks) {
        val angle = (i * 360f / totalTicks) * PI.toFloat() / 180f
        val isMajor = i % 5 == 0
        val tickInner = if (isMajor) innerR * 1.02f else gapRadius - 2f
        val tickOuter = outerR * 0.97f

        val sx = cx + cos(angle) * tickInner
        val sy = cy + sin(angle) * tickInner
        val ex = cx + cos(angle) * tickOuter
        val ey = cy + sin(angle) * tickOuter

        drawLine(
            color = if (isMajor) tickColorBright else tickColor,
            start = Offset(sx, sy),
            end = Offset(ex, ey),
            strokeWidth = if (isMajor) 2f else 1f,
            cap = StrokeCap.Butt
        )
    }
}

private fun DrawScope.drawInnerTechRing(
    cx: Float, cy: Float, ringRadius: Float, alpha: Float
) {
    val arcColor = NeonCyan.copy(alpha = alpha * 0.45f)
    val arcColorBright = NeonCyan.copy(alpha = alpha * 0.7f)

    // Several segmented arcs at different positions
    val segments = listOf(
        // startAngle, sweepAngle, thick?
        Triple(10f, 35f, false),
        Triple(55f, 20f, true),
        Triple(100f, 45f, false),
        Triple(160f, 25f, true),
        Triple(200f, 40f, false),
        Triple(260f, 15f, true),
        Triple(290f, 50f, false),
        Triple(350f, 8f, true)
    )

    for ((start, sweep, thick) in segments) {
        drawArc(
            color = if (thick) arcColorBright else arcColor,
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(cx - ringRadius, cy - ringRadius),
            size = androidx.compose.ui.geometry.Size(ringRadius * 2, ringRadius * 2),
            style = Stroke(width = if (thick) 3f else 1.5f, cap = StrokeCap.Butt)
        )
    }

    // Small notch marks on the inner ring
    val notches = listOf(0f, 30f, 90f, 150f, 210f, 270f, 330f)
    for (angle in notches) {
        val rad = angle * PI.toFloat() / 180f
        val inner = ringRadius - 5f
        val outer = ringRadius + 5f
        drawLine(
            color = arcColor,
            start = Offset(cx + cos(rad) * inner, cy + sin(rad) * inner),
            end = Offset(cx + cos(rad) * outer, cy + sin(rad) * outer),
            strokeWidth = 1.5f
        )
    }
}

private fun DrawScope.drawScanline(
    cx: Float, cy: Float, outerR: Float, innerR: Float,
    angle: Float, alpha: Float
) {
    val rad = angle * PI.toFloat() / 180f
    val sweepRad = 15f * PI.toFloat() / 180f

    // Draw a fading wedge as scanline
    val scanPath = Path().apply {
        moveTo(cx, cy)
        val steps = 8
        for (i in 0..steps) {
            val a = rad + sweepRad * i / steps
            lineTo(cx + cos(a) * outerR, cy + sin(a) * outerR)
        }
        close()
    }
    drawPath(
        path = scanPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                NeonCyan.copy(alpha = alpha * 0.06f),
                NeonCyan.copy(alpha = alpha * 0.12f)
            ),
            center = Offset(cx, cy),
            radius = outerR
        )
    )
}

private fun DrawScope.drawChevron(cx: Float, cy: Float, alpha: Float) {
    val chevronColor = NeonCyan.copy(alpha = alpha * 0.8f)
    val halfW = 10.dp.toPx()
    val h = 6.dp.toPx()

    val path = Path().apply {
        moveTo(cx - halfW, cy + h)
        lineTo(cx, cy)
        lineTo(cx + halfW, cy + h)
    }
    drawPath(
        path = path,
        color = chevronColor,
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
}
