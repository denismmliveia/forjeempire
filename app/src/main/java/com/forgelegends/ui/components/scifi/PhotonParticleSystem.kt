package com.forgelegends.ui.components.scifi

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.HoloPurple
import com.forgelegends.ui.theme.NeonCyan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

internal class Photon(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    val decayRate: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun PhotonParticleSystem(
    modifier: Modifier = Modifier,
    particleCount: Int = 25
) {
    // Plain mutable list — no State wrapper, no allocations per frame
    val particles = remember { mutableListOf<Photon>() }
    val colors = remember { listOf(NeonCyan, ElectricBlue, HoloPurple, NeonCyan.copy(alpha = 0.7f)) }

    // Frame nanos as state to trigger Canvas invalidation
    var frameNanos by remember { mutableLongStateOf(0L) }
    var lastNanos by remember { mutableLongStateOf(0L) }
    var canvasW by remember { mutableLongStateOf(0L) }
    var canvasH by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameNanos { nanos ->
                if (lastNanos == 0L) { lastNanos = nanos; return@withInfiniteAnimationFrameNanos }

                val dtSec = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = nanos

                val w = canvasW / 1000f
                val h = canvasH / 1000f

                // Remove dead
                particles.removeAll { it.alpha <= 0.005f }

                // Spawn
                while (particles.size < particleCount && w > 0f) {
                    particles.add(spawnAmbientPhoton(w, h, colors))
                }

                // Update in-place
                for (p in particles) {
                    p.x += p.vx * dtSec * 60f
                    p.y += p.vy * dtSec * 60f
                    p.alpha = (p.alpha - p.decayRate * dtSec * 60f).coerceAtLeast(0f)

                    if (p.x < -20f) p.x = w + 20f
                    if (p.x > w + 20f) p.x = -20f
                }

                frameNanos = nanos // triggers Canvas redraw
            }
        }
    }

    Canvas(modifier = modifier) {
        // Store canvas size for the coroutine (encoded as Long to avoid Float state)
        canvasW = (size.width * 1000f).toLong()
        canvasH = (size.height * 1000f).toLong()

        // Read frameNanos to subscribe to updates
        @Suppress("UNUSED_VARIABLE")
        val tick = frameNanos

        for (p in particles) {
            if (p.alpha <= 0f) continue

            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        p.color.copy(alpha = p.alpha * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(p.x, p.y),
                    radius = p.radius * 3f
                ),
                radius = p.radius * 3f,
                center = Offset(p.x, p.y)
            )

            // Core
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(p.x, p.y)
            )
        }
    }
}

internal fun spawnBurst(
    centerX: Float,
    centerY: Float,
    count: Int = 12
): List<Photon> {
    val colors = listOf(NeonCyan, ElectricBlue, HoloPurple)
    return List(count) {
        val angle = Random.nextFloat() * 2f * PI.toFloat()
        val speed = Random.nextFloat() * 3f + 1f
        Photon(
            x = centerX,
            y = centerY,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            alpha = 1f,
            decayRate = Random.nextFloat() * 0.015f + 0.01f,
            radius = Random.nextFloat() * 3f + 1.5f,
            color = colors.random()
        )
    }
}

private fun spawnAmbientPhoton(
    width: Float,
    height: Float,
    colors: List<Color>
): Photon {
    val angle = Random.nextFloat() * 2f * PI.toFloat()
    return Photon(
        x = Random.nextFloat() * width,
        y = Random.nextFloat() * height,
        vx = cos(angle) * (Random.nextFloat() * 0.4f + 0.1f),
        vy = -Random.nextFloat() * 0.5f - 0.15f,
        alpha = Random.nextFloat() * 0.5f + 0.2f,
        decayRate = Random.nextFloat() * 0.003f + 0.001f,
        radius = Random.nextFloat() * 2.5f + 1f,
        color = colors.random()
    )
}
