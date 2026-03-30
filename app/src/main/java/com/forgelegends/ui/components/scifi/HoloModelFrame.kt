package com.forgelegends.ui.components.scifi

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.random.Random

/**
 * Holographic display frame drawn around 3D model images.
 *
 * Visual elements (based on reference image):
 *  - Metallic outer border with beveled highlight (steel gradient, top-lit)
 *  - Inner thin neon cyan rectangle (like a screen bezel)
 *  - 8 thin geometric lines crossing through the interior from edge points
 *  - 4 neon indicator bars at mid-edges (slow pulse animation)
 *  - Scattered particle sparkles
 *  - Corner accent glows
 */
@Composable
fun HoloModelFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Slow indicator pulse (0.35 → 1.0)
    val infiniteTransition = rememberInfiniteTransition(label = "modelFrame")
    val indicatorPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "indicatorPulse"
    )

    // Secondary slower pulse offset for variety
    val scanPulse by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanPulse"
    )

    // Fixed pseudo-random particle positions (stable across recompositions)
    val particles = remember {
        val rng = Random(42)
        List(28) {
            Triple(
                rng.nextFloat(),   // normalised x
                rng.nextFloat(),   // normalised y
                rng.nextFloat()    // size/brightness factor
            )
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Frame drawn behind content
        Canvas(modifier = Modifier.matchParentSize()) {
            drawModelFrame(
                indicatorPulse = indicatorPulse,
                scanPulse = scanPulse,
                particles = particles
            )
        }

        // Content (model image) sits inside the frame, inset from the border
        Box(
            modifier = Modifier.padding(13.dp),
            contentAlignment = Alignment.Center,
            content = content
        )

        // Frame overlay (lines + glow drawn on top of content at low alpha)
        Canvas(modifier = Modifier.matchParentSize()) {
            drawModelFrameOverlay(
                indicatorPulse = indicatorPulse,
                scanPulse = scanPulse
            )
        }
    }
}

// ─── Drawing helpers ────────────────────────────────────────────────────────────

private fun DrawScope.drawModelFrame(
    indicatorPulse: Float,
    scanPulse: Float,
    particles: List<Triple<Float, Float, Float>>
) {
    val w = size.width
    val h = size.height
    val borderW = 10.dp.toPx()   // metallic outer border width
    val innerGap = 3.dp.toPx()   // gap between metal and neon line
    val neonLineW = 1.3.dp.toPx()

    // ── 1 · Dark interior background (behind content) ──
    // This fills the full area so frame has a solid base
    drawRect(Color(0xFF000C18))
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF002244).copy(alpha = 0.6f), Color.Transparent),
            center = Offset(w / 2f, h / 2f),
            radius = maxOf(w, h) * 0.6f
        )
    )

    // ── 2 · Metallic outer border ──
    // Top edge (brightest — top-lit steel)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFD0DDE8), Color(0xFF5A7080)),
            startY = 0f, endY = borderW
        ),
        size = Size(w, borderW)
    )
    // Bottom edge
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF2A3A48), Color(0xFF182430)),
            startY = h - borderW, endY = h
        ),
        topLeft = Offset(0f, h - borderW),
        size = Size(w, borderW)
    )
    // Left edge
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF8899AA), Color(0xFF3A4E5C)),
            startX = 0f, endX = borderW
        ),
        size = Size(borderW, h)
    )
    // Right edge
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF2A3A48), Color(0xFF1A2A38)),
            startX = w - borderW, endX = w
        ),
        topLeft = Offset(w - borderW, 0f),
        size = Size(borderW, h)
    )

    // Corner bevels (diagonal gradient patches)
    val bevelPath = Path()
    // Top-left corner bevel
    bevelPath.apply {
        moveTo(0f, 0f); lineTo(borderW, 0f); lineTo(borderW, borderW); lineTo(0f, borderW); close()
    }
    drawPath(bevelPath, brush = Brush.linearGradient(
        colors = listOf(Color(0xFFCCDDEE), Color(0xFF4A5E6E)),
        start = Offset(0f, 0f), end = Offset(borderW, borderW)
    ))

    // Inner bevel highlight line (top edge)
    drawLine(
        color = Color(0xFFE8F4FF).copy(alpha = 0.9f),
        start = Offset(borderW, borderW * 0.35f),
        end = Offset(w - borderW, borderW * 0.35f),
        strokeWidth = 1.dp.toPx()
    )
    // Inner bevel shadow line (bottom edge)
    drawLine(
        color = Color(0xFF000811).copy(alpha = 0.8f),
        start = Offset(borderW, h - borderW * 0.5f),
        end = Offset(w - borderW, h - borderW * 0.5f),
        strokeWidth = 1.5.dp.toPx()
    )

    // ── 3 · Outer corner glow accents ──
    val cornerGlowColor = Color(0xFF0099FF).copy(alpha = 0.4f * indicatorPulse)
    listOf(
        Offset(borderW * 0.5f, borderW * 0.5f),
        Offset(w - borderW * 0.5f, borderW * 0.5f),
        Offset(borderW * 0.5f, h - borderW * 0.5f),
        Offset(w - borderW * 0.5f, h - borderW * 0.5f)
    ).forEach { pos ->
        drawCircle(cornerGlowColor, 8.dp.toPx(), pos)
    }

    // ── 4 · Particles (sparkles) ──
    val innerLeft = borderW + innerGap
    val innerTop = borderW + innerGap
    val innerRight = w - borderW - innerGap
    val innerBottom = h - borderW - innerGap
    val innerW = innerRight - innerLeft
    val innerH = innerBottom - innerTop

    particles.forEach { (nx, ny, nf) ->
        val px = innerLeft + nx * innerW
        val py = innerTop + ny * innerH
        val r = (1.2f + nf * 2.2f).dp.toPx()
        val brightness = 0.25f + nf * 0.6f
        drawCircle(
            color = Color(0xFF88DDFF).copy(alpha = brightness * (0.5f + scanPulse * 0.5f)),
            radius = r,
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawModelFrameOverlay(
    indicatorPulse: Float,
    scanPulse: Float
) {
    val w = size.width
    val h = size.height
    val borderW = 10.dp.toPx()
    val innerGap = 3.dp.toPx()
    val neonLineW = 1.3.dp.toPx()

    val innerRect = Rect(
        left = borderW + innerGap,
        top = borderW + innerGap,
        right = w - borderW - innerGap,
        bottom = h - borderW - innerGap
    )

    // ── 5 · Inner neon cyan border rectangle ──
    val neonColor = Color(0xFF00AAFF).copy(alpha = 0.85f * indicatorPulse)
    val neonGlow = Color(0xFF0066DD).copy(alpha = 0.30f * indicatorPulse)

    // Glow pass (wide, soft)
    drawRect(
        color = neonGlow,
        topLeft = Offset(innerRect.left - 2.dp.toPx(), innerRect.top - 2.dp.toPx()),
        size = Size(innerRect.width + 4.dp.toPx(), innerRect.height + 4.dp.toPx()),
        style = Stroke(width = 4.dp.toPx())
    )
    // Core pass (sharp line)
    drawRect(
        color = neonColor,
        topLeft = Offset(innerRect.left, innerRect.top),
        size = Size(innerRect.width, innerRect.height),
        style = Stroke(width = neonLineW)
    )

    // ── 6 · Mid-edge neon indicator bars ──
    val barLen = 18.dp.toPx()
    val barAlpha = indicatorPulse
    val barColor = Color(0xFF00CCFF).copy(alpha = barAlpha)
    val barGlow  = Color(0xFF0088FF).copy(alpha = barAlpha * 0.4f)
    val barStroke = Stroke(width = 2.dp.toPx())
    val barGlowStroke = Stroke(width = 5.dp.toPx())

    // Top-center bar
    val topMidX = w / 2f
    drawLine(barGlow, Offset(topMidX - barLen / 2f, innerRect.top), Offset(topMidX + barLen / 2f, innerRect.top), 5.dp.toPx())
    drawLine(barColor, Offset(topMidX - barLen / 2f, innerRect.top), Offset(topMidX + barLen / 2f, innerRect.top), 2.dp.toPx())

    // Bottom-center bar
    val botMidX = w / 2f
    drawLine(barGlow, Offset(botMidX - barLen / 2f, innerRect.bottom), Offset(botMidX + barLen / 2f, innerRect.bottom), 5.dp.toPx())
    drawLine(barColor, Offset(botMidX - barLen / 2f, innerRect.bottom), Offset(botMidX + barLen / 2f, innerRect.bottom), 2.dp.toPx())

    // Left-center bar (vertical)
    val leftMidY = h / 2f
    drawLine(barGlow, Offset(innerRect.left, leftMidY - barLen / 2f), Offset(innerRect.left, leftMidY + barLen / 2f), 5.dp.toPx())
    drawLine(barColor, Offset(innerRect.left, leftMidY - barLen / 2f), Offset(innerRect.left, leftMidY + barLen / 2f), 2.dp.toPx())

    // Right-center bar (vertical)
    val rightMidY = h / 2f
    drawLine(barGlow, Offset(innerRect.right, rightMidY - barLen / 2f), Offset(innerRect.right, rightMidY + barLen / 2f), 5.dp.toPx())
    drawLine(barColor, Offset(innerRect.right, rightMidY - barLen / 2f), Offset(innerRect.right, rightMidY + barLen / 2f), 2.dp.toPx())

    // ── 7 · Geometric scan lines (overlay on model at very low alpha) ──
    val lineAlpha = 0.18f + scanPulse * 0.08f
    val lineColor = Color(0xFF55AADD).copy(alpha = lineAlpha)
    val thinStroke = 0.7.dp.toPx()

    // Star-burst lines converging near center with slight offset
    val cx = w * 0.50f
    val cy = h * 0.48f

    val edgePoints = listOf(
        Offset(innerRect.left, innerRect.top + innerRect.height * 0.25f),
        Offset(innerRect.left, innerRect.top + innerRect.height * 0.72f),
        Offset(innerRect.left + innerRect.width * 0.18f, innerRect.top),
        Offset(innerRect.left + innerRect.width * 0.65f, innerRect.top),
        Offset(innerRect.right, innerRect.top + innerRect.height * 0.30f),
        Offset(innerRect.right, innerRect.top + innerRect.height * 0.68f),
        Offset(innerRect.left + innerRect.width * 0.35f, innerRect.bottom),
        Offset(innerRect.left + innerRect.width * 0.78f, innerRect.bottom),
    )

    // Clip lines to inner frame area
    val clipPath = Path().apply {
        addRect(innerRect)
    }
    clipPath(clipPath) {
        edgePoints.forEachIndexed { i, ep ->
            // Each line converges near center with small offset
            val targetX = cx + (if (i % 2 == 0) 8.dp.toPx() else -8.dp.toPx())
            val targetY = cy + (if (i % 3 == 0) 6.dp.toPx() else -6.dp.toPx())
            drawLine(lineColor, ep, Offset(targetX, targetY), thinStroke)
        }

        // Inner smaller rectangle (circuit trace)
        val ir2 = innerRect.inflate(-14.dp.toPx())
        drawRect(
            color = Color(0xFF003366).copy(alpha = 0.35f + scanPulse * 0.15f),
            topLeft = Offset(ir2.left, ir2.top),
            size = Size(ir2.width, ir2.height),
            style = Stroke(width = 0.6.dp.toPx())
        )
    }
}
