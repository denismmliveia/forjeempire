package com.forgelegends.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.forgelegends.ui.theme.NeonCyan
import kotlinx.coroutines.delay

object PhaseImageProvider {

    fun phaseImagePath(conceptId: String, phase: Int): String =
        "concepts/$conceptId/phase_${phase.coerceIn(1, 6)}.png"

    fun hasPhaseImages(context: Context, conceptId: String): Boolean {
        return try {
            context.assets.open(phaseImagePath(conceptId, 1)).close()
            true
        } catch (_: Exception) {
            false
        }
    }

    @Composable
    fun PhaseImage(
        conceptId: String,
        phase: Int,
        modifier: Modifier = Modifier,
        contentScale: ContentScale = ContentScale.Fit
    ) {
        val context = LocalContext.current

        // Track phase changes for flash effect
        var showFlash by remember { mutableStateOf(false) }
        var prevPhase by remember { mutableStateOf(phase) }

        LaunchedEffect(phase) {
            if (phase != prevPhase) {
                showFlash = true
                delay(400)
                showFlash = false
            }
            prevPhase = phase
        }

        val flashAlpha by animateFloatAsState(
            targetValue = if (showFlash) 1f else 0f,
            animationSpec = if (showFlash) tween(80) else tween(350),
            label = "flash"
        )

        Box(modifier = modifier) {
            AnimatedContent(
                targetState = phase,
                transitionSpec = {
                    fadeIn(tween(500, delayMillis = 100)) togetherWith fadeOut(tween(200))
                },
                label = "phase-transition"
            ) { targetPhase ->
                val bitmap = remember(conceptId, targetPhase) {
                    try {
                        context.assets.open(phaseImagePath(conceptId, targetPhase)).use {
                            BitmapFactory.decodeStream(it)
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Layer $targetPhase",
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Energy flash overlay
            if (flashAlpha > 0.01f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Central bright flash
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = flashAlpha * 0.9f),
                                NeonCyan.copy(alpha = flashAlpha * 0.6f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.maxDimension * 0.6f
                        ),
                        radius = size.maxDimension * 0.6f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )

                    // Horizontal scan line
                    val scanY = size.height * (0.3f + flashAlpha * 0.4f)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                NeonCyan.copy(alpha = flashAlpha * 0.8f),
                                Color.White.copy(alpha = flashAlpha * 0.5f),
                                NeonCyan.copy(alpha = flashAlpha * 0.8f),
                                Color.Transparent
                            ),
                            startY = scanY - 15f,
                            endY = scanY + 15f
                        )
                    )
                }
            }
        }
    }
}
