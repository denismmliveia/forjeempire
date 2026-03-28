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
import com.forgelegends.domain.model.WeaponFamily

object PhaseImageProvider {

    fun phaseImagePath(family: WeaponFamily, phase: Int): String {
        val familyDir = family.name.lowercase()
        return "phases/$familyDir/${familyDir}_phase${phase.coerceIn(1, 6)}.png"
    }

    fun hasPhaseImages(context: Context, family: WeaponFamily): Boolean {
        return try {
            context.assets.open(phaseImagePath(family, 1)).close()
            true
        } catch (_: Exception) {
            false
        }
    }

    @Composable
    fun PhaseImage(
        family: WeaponFamily,
        phase: Int,
        modifier: Modifier = Modifier,
        contentScale: ContentScale = ContentScale.Fit
    ) {
        val context = LocalContext.current
        val bitmap = remember(family, phase) {
            try {
                context.assets.open(phaseImagePath(family, phase)).use {
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
