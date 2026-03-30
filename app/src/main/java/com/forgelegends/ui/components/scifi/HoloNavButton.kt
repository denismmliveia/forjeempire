package com.forgelegends.ui.components.scifi

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import kotlin.random.Random
import androidx.compose.material3.Text

/**
 * Sci-fi navigation button with metallic cut-corner frame,
 * deep-blue crystal interior, facet lines and animated lightning bolts.
 *
 * Matches the aesthetic of the reference "MENU" button concept art.
 */
@Composable
fun HoloNavButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Press scale bounce
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navBtnScale"
    )

    // Lightning bolt seed — incremented slowly (subtle, occasional effect)
    var lightSeed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(420L)   // slower: bolts reappear every ~420 ms
            lightSeed++
        }
    }

    // Slow glow pulse (50% → 100% intensity)
    val infiniteTransition = rememberInfiniteTransition(label = "holoNav")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    // Extra press flash
    val pressGlow by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(80),
        label = "pressGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background: frame + crystal + lightning (Canvas)
        Canvas(modifier = Modifier.matchParentSize()) {
            drawHoloNavFrame(
                glowPulse = glowPulse,
                pressGlow = pressGlow,
                lightSeed = lightSeed
            )
        }

        // Text overlay (supports emoji, renders above canvas)
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                shadow = Shadow(
                    color = Color(0xFF00AAFF),
                    blurRadius = 16f
                )
            )
        )
    }
}

// ─── Canvas drawing ────────────────────────────────────────────────────────────

private fun DrawScope.drawHoloNavFrame(
    glowPulse: Float,
    pressGlow: Float,
    lightSeed: Long
) {
    val w = size.width
    val h = size.height
    val cut = 16.dp.toPx()           // corner cut size
    val frameStroke = 3.5.dp.toPx() // metallic border width
    val intensity = (glowPulse + pressGlow * 0.6f).coerceIn(0f, 1.6f)

    val outerPath = buildCutPath(0f, 0f, w, h, cut)
    val innerInset = frameStroke + 1.dp.toPx()
    val innerCut = (cut - innerInset).coerceAtLeast(2.dp.toPx())
    val innerPath = buildCutPath(innerInset, innerInset, w - innerInset, h - innerInset, innerCut)

    // 1 · Outer bloom glow (extends beyond button silhouette)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF00BBFF).copy(alpha = 0.35f * intensity),
                Color(0xFF0055BB).copy(alpha = 0.12f * intensity),
                Color.Transparent
            ),
            center = Offset(w / 2f, h / 2f),
            radius = w * 0.72f
        )
    )

    // 2 · Crystal interior fill
    clipPath(innerPath) {
        // Dark void base
        drawRect(Color(0xFF000E1C))

        // Center radial blue glow
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0066BB).copy(alpha = 0.95f),
                    Color(0xFF001133).copy(alpha = 0f)
                ),
                center = Offset(w / 2f, h / 2f),
                radius = h * 0.9f
            )
        )

        // Diagonal gradient sweep (like a faceted gem catching light)
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0099DD).copy(alpha = 0.18f),
                    Color.Transparent,
                    Color(0xFF003366).copy(alpha = 0.12f)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        // Facet lines (diagonal crystal cuts)
        val fStroke = 0.9.dp.toPx()
        val fc1 = Color(0xFF00AAFF).copy(alpha = 0.22f)
        val fc2 = Color(0xFF004488).copy(alpha = 0.28f)
        drawLine(fc1, Offset(w * 0.18f, 0f), Offset(w * 0.50f, h), fStroke)
        drawLine(fc1, Offset(w * 0.42f, 0f), Offset(w * 0.74f, h), fStroke)
        drawLine(fc2, Offset(w * 0.62f, 0f), Offset(w * 0.30f, h), fStroke)
        drawLine(fc2, Offset(w * 0.82f, 0f), Offset(w * 0.50f, h), fStroke)
        // Extra thin highlights
        drawLine(Color(0xFF55CCFF).copy(alpha = 0.10f), Offset(w * 0.30f, 0f), Offset(w * 0.60f, h), fStroke * 0.6f)

        // Top-edge shine (reflective surface highlight)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF55DDFF).copy(alpha = 0.18f * intensity),
                    Color.Transparent
                ),
                startY = 0f,
                endY = h * 0.45f
            )
        )
    }

    // 3 · Metallic frame border (steel gradient top-light)
    drawPath(
        path = outerPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFCCDDEE), // bright steel top
                Color(0xFF8899BB),
                Color(0xFF445566),
                Color(0xFF1E2D3D)  // dark steel bottom
            ),
            start = Offset(0f, 0f),
            end = Offset(w * 0.25f, h)
        ),
        style = Stroke(width = frameStroke)
    )

    // Top bevel highlight line (bright glint)
    drawLine(
        color = Color(0xFFAADDFF).copy(alpha = 0.85f),
        start = Offset(cut + 4f, 1.dp.toPx()),
        end = Offset(w - cut - 4f, 1.dp.toPx()),
        strokeWidth = 1.dp.toPx()
    )

    // Bottom shadow line
    drawLine(
        color = Color(0xFF000A18).copy(alpha = 0.9f),
        start = Offset(cut + 4f, h - 1.2.dp.toPx()),
        end = Offset(w - cut - 4f, h - 1.2.dp.toPx()),
        strokeWidth = 1.dp.toPx()
    )

    // Corner accent dots (glowing)
    val dotR = 2.5.dp.toPx()
    val dotColor = Color(0xFF00CCFF).copy(alpha = 0.95f * intensity)
    val dotGlowColor = Color(0xFF00AAFF).copy(alpha = 0.35f * intensity)
    listOf(
        Offset(cut * 0.55f, cut * 0.55f),
        Offset(w - cut * 0.55f, cut * 0.55f),
        Offset(cut * 0.55f, h - cut * 0.55f),
        Offset(w - cut * 0.55f, h - cut * 0.55f)
    ).forEach { pos ->
        drawCircle(dotGlowColor, dotR * 3f, pos)
        drawCircle(dotColor, dotR, pos)
    }

    // 4 · Lightning bolts
    val rng = Random(lightSeed)
    val lc = Color(0xFF00CCFF).copy(alpha = 0.90f * intensity)

    // Main bolts — only show ~60% of the time for subtlety
    if (rng.nextFloat() > 0.40f) {
        drawLightningBolt(
            rng = rng, color = lc.copy(alpha = lc.alpha * 0.75f),
            from = Offset(-14f, h / 2f + rng.nextFloat() * 4f - 2f),
            to = Offset(2f, h / 2f + (rng.nextFloat() - 0.5f) * h * 0.4f),
            segments = 5
        )
    }
    if (rng.nextFloat() > 0.40f) {
        drawLightningBolt(
            rng = rng, color = lc.copy(alpha = lc.alpha * 0.75f),
            from = Offset(w + 14f, h / 2f + rng.nextFloat() * 4f - 2f),
            to = Offset(w - 2f, h / 2f + (rng.nextFloat() - 0.5f) * h * 0.4f),
            segments = 5
        )
    }

    // Rare corner spark (~25% of frames, very faint)
    if (rng.nextFloat() > 0.75f) {
        drawLightningBolt(
            rng = rng, color = lc.copy(alpha = lc.alpha * 0.38f),
            from = Offset(w + 6f, -4f),
            to = Offset(w - cut, cut),
            segments = 3
        )
    }
}

/** Builds an octagonal path with 45° cut corners. */
private fun buildCutPath(
    left: Float, top: Float, right: Float, bottom: Float, cut: Float
): Path = Path().apply {
    moveTo(left + cut, top)
    lineTo(right - cut, top)
    lineTo(right, top + cut)
    lineTo(right, bottom - cut)
    lineTo(right - cut, bottom)
    lineTo(left + cut, bottom)
    lineTo(left, bottom - cut)
    lineTo(left, top + cut)
    close()
}

/** Draws a zigzag lightning bolt between two points with outer bloom + inner core. */
private fun DrawScope.drawLightningBolt(
    rng: Random,
    color: Color,
    from: Offset,
    to: Offset,
    segments: Int
) {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
    // Perpendicular unit vector for jitter
    val perpX = -dy / len
    val perpY = dx / len
    val perpScale = len * 0.28f

    val path = Path()
    path.moveTo(from.x, from.y)
    for (i in 1 until segments) {
        val t = i.toFloat() / segments
        val jitter = (rng.nextFloat() - 0.5f) * perpScale
        path.lineTo(
            from.x + dx * t + perpX * jitter,
            from.y + dy * t + perpY * jitter
        )
    }
    path.lineTo(to.x, to.y)

    // Wide bloom pass (soft glow halo)
    drawPath(
        path = path,
        color = color.copy(alpha = color.alpha * 0.22f),
        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    // Narrow core pass (bright center line)
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}
