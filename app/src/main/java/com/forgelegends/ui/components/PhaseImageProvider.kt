package com.forgelegends.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

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
        val bitmap = remember(conceptId, phase) {
            try {
                context.assets.open(phaseImagePath(conceptId, phase)).use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) {
                null
            }
        }
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Phase $phase",
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}
