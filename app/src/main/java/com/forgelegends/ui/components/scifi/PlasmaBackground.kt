package com.forgelegends.ui.components.scifi

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import kotlin.math.sin
import kotlin.math.cos

/**
 * Smooth procedural plasma background running at display refresh rate.
 * No bitmaps, no spritesheet — pure Canvas drawing driven by time.
 * Uses a single Float state so Compose only does one state read per frame.
 */
@Composable
fun PlasmaBackground(
    modifier: Modifier = Modifier,
    alpha: Float = 0.18f
) {
    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withInfiniteAnimationFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val deltaSeconds = ((nanos - lastNanos) / 1_000_000_000f)
                        .coerceIn(0f, 0.05f)
                    time += deltaSeconds
                }
                lastNanos = nanos
            }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 4 slow-moving cyan radial blobs — each on a different period
        val blobs = listOf(
            // (x_fn, y_fn, radius_mult, color_alpha)
            Triple(
                Offset(
                    w * (0.5f + 0.35f * sin(time * 0.18f)),
                    h * (0.4f + 0.25f * cos(time * 0.13f))
                ),
                w * 0.6f,
                alpha * 0.9f
            ),
            Triple(
                Offset(
                    w * (0.3f + 0.3f * cos(time * 0.11f + 1.2f)),
                    h * (0.55f + 0.2f * sin(time * 0.09f + 0.7f))
                ),
                w * 0.5f,
                alpha * 0.7f
            ),
            Triple(
                Offset(
                    w * (0.7f + 0.25f * sin(time * 0.14f + 2.1f)),
                    h * (0.3f + 0.3f * cos(time * 0.16f + 1.5f))
                ),
                w * 0.45f,
                alpha * 0.6f
            ),
            Triple(
                Offset(
                    w * (0.5f + 0.4f * cos(time * 0.07f + 0.5f)),
                    h * (0.7f + 0.2f * sin(time * 0.12f + 3.0f))
                ),
                w * 0.55f,
                alpha * 0.5f
            ),
        )

        val cyan = Color(0xFF00F0FF)
        val teal = Color(0xFF006080)

        for ((center, radius, blobAlpha) in blobs) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        teal.copy(alpha = blobAlpha * 0.6f),
                        cyan.copy(alpha = blobAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // Slow diagonal sweep for extra depth
        val sweepX = w * (0.5f + 0.5f * sin(time * 0.05f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    cyan.copy(alpha = alpha * 0.12f),
                    Color.Transparent
                ),
                center = Offset(sweepX, h * 0.5f),
                radius = w * 0.8f
            ),
            radius = w * 0.8f,
            center = Offset(sweepX, h * 0.5f)
        )
    }
}
