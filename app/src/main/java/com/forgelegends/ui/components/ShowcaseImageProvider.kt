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

object ShowcaseImageProvider {

    private fun anglePath(conceptId: String, angle: Int): String =
        "concepts/$conceptId/angle_$angle.png"

    fun angleCount(context: Context, conceptId: String): Int {
        var count = 0
        while (count < 24) {
            try {
                context.assets.open(anglePath(conceptId, count)).close()
                count++
            } catch (_: Exception) {
                break
            }
        }
        return count
    }

    @Composable
    fun AngleImage(
        conceptId: String,
        angle: Int,
        modifier: Modifier = Modifier,
        contentScale: ContentScale = ContentScale.Fit
    ) {
        val context = LocalContext.current
        val bitmap = remember(conceptId, angle) {
            try {
                context.assets.open(anglePath(conceptId, angle)).use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) {
                null
            }
        }
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Angle $angle",
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}
