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

object ShowcaseImageProvider {

    private fun anglePath(family: WeaponFamily, angle: Int): String {
        val familyDir = family.name.lowercase()
        return "showcase/$familyDir/angle_$angle.png"
    }

    fun angleCount(context: Context, family: WeaponFamily): Int {
        var count = 0
        while (count < 24) {
            try {
                context.assets.open(anglePath(family, count)).close()
                count++
            } catch (_: Exception) {
                break
            }
        }
        return count
    }

    @Composable
    fun AngleImage(
        family: WeaponFamily,
        angle: Int,
        modifier: Modifier = Modifier,
        contentScale: ContentScale = ContentScale.Fit
    ) {
        val context = LocalContext.current
        val bitmap = remember(family, angle) {
            try {
                context.assets.open(anglePath(family, angle)).use {
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
