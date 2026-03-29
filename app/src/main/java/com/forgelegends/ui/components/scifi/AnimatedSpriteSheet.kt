package com.forgelegends.ui.components.scifi

import android.graphics.BitmapFactory
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject

/**
 * Renders an animated sprite sheet from assets.
 *
 * Expects:
 * - A PNG atlas at [sheetAssetPath] (e.g. "textures/plasma_bg_sheet.png")
 * - A JSON metadata file at [metadataAssetPath] with: cols, rows, frameCount, frameWidth, frameHeight
 *
 * Uses withInfiniteAnimationFrameNanos for smooth frame cycling (same pattern as PhotonParticleSystem).
 */
@Composable
fun AnimatedSpriteSheet(
    sheetAssetPath: String,
    metadataAssetPath: String,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    fps: Int = 24,
    loop: Boolean = true,
    playing: Boolean = true,
    onComplete: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Load bitmap and metadata once
    val sheetData = remember(sheetAssetPath, metadataAssetPath) {
        try {
            val bitmap = context.assets.open(sheetAssetPath).use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@remember null

            val meta = context.assets.open(metadataAssetPath).use { stream ->
                JSONObject(stream.bufferedReader().readText())
            }

            SpriteSheetData(
                bitmap = bitmap,
                cols = meta.getInt("cols"),
                rows = meta.getInt("rows"),
                frameCount = meta.getInt("frameCount"),
                frameWidth = meta.getInt("frameWidth"),
                frameHeight = meta.getInt("frameHeight")
            )
        } catch (_: Exception) {
            null
        }
    } ?: return

    val imageBitmap = remember(sheetData) { sheetData.bitmap.asImageBitmap() }

    // Frame index driven by animation clock
    var currentFrame by remember { mutableIntStateOf(0) }
    var frameNanos by remember { mutableLongStateOf(0L) }
    val frameDurationNanos = remember(fps) { 1_000_000_000L / fps.toLong() }
    var accumNanos by remember { mutableLongStateOf(0L) }
    var lastNanos by remember { mutableLongStateOf(0L) }
    var completed by remember { mutableIntStateOf(0) }

    LaunchedEffect(playing) {
        if (!playing) return@LaunchedEffect
        lastNanos = 0L
        accumNanos = 0L
        currentFrame = 0
        while (true) {
            withInfiniteAnimationFrameNanos { nanos ->
                if (lastNanos == 0L) {
                    lastNanos = nanos
                    return@withInfiniteAnimationFrameNanos
                }
                val delta = (nanos - lastNanos).coerceIn(0L, 100_000_000L) // cap at 100ms
                lastNanos = nanos
                accumNanos += delta

                if (accumNanos >= frameDurationNanos) {
                    val steps = (accumNanos / frameDurationNanos).toInt()
                    accumNanos %= frameDurationNanos
                    var next = currentFrame + steps

                    if (next >= sheetData.frameCount) {
                        if (loop) {
                            next %= sheetData.frameCount
                        } else {
                            next = sheetData.frameCount - 1
                            if (completed == 0) {
                                completed = 1
                                onComplete?.invoke()
                            }
                        }
                    }
                    currentFrame = next
                }
                frameNanos = nanos
            }
        }
    }

    Canvas(modifier = modifier) {
        // Read frameNanos to subscribe to updates
        @Suppress("UNUSED_VARIABLE")
        val tick = frameNanos

        val col = currentFrame % sheetData.cols
        val row = currentFrame / sheetData.cols
        val srcLeft = col * sheetData.frameWidth
        val srcTop = row * sheetData.frameHeight

        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                this.alpha = (alpha * 255).toInt()
                isFilterBitmap = true
            }
            canvas.nativeCanvas.drawBitmap(
                sheetData.bitmap,
                android.graphics.Rect(
                    srcLeft, srcTop,
                    srcLeft + sheetData.frameWidth,
                    srcTop + sheetData.frameHeight
                ),
                android.graphics.RectF(0f, 0f, size.width, size.height),
                paint
            )
        }
    }
}

private class SpriteSheetData(
    val bitmap: android.graphics.Bitmap,
    val cols: Int,
    val rows: Int,
    val frameCount: Int,
    val frameWidth: Int,
    val frameHeight: Int
)
