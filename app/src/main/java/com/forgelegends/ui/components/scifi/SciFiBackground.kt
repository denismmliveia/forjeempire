package com.forgelegends.ui.components.scifi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.forgelegends.ui.theme.CyanGlow
import com.forgelegends.ui.theme.DeepSpace
import com.forgelegends.ui.theme.NebulaSurface
import com.forgelegends.ui.theme.VoidBlack

@Composable
fun SciFiBackground(
    modifier: Modifier = Modifier,
    showRadialGlow: Boolean = false,
    showAnimatedTexture: Boolean = false,
    glowColor: Color = CyanGlow,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VoidBlack, DeepSpace, NebulaSurface)
                )
            )
            .then(
                if (showRadialGlow) {
                    Modifier.drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(glowColor, Color.Transparent),
                                center = Offset(size.width / 2f, size.height * 0.35f),
                                radius = size.width * 0.7f
                            )
                        )
                    }
                } else Modifier
            )
    ) {
        // Animated plasma texture overlay (subtle, behind content)
        if (showAnimatedTexture) {
            AnimatedSpriteSheet(
                sheetAssetPath = "textures/plasma_bg_sheet.png",
                metadataAssetPath = "textures/plasma_bg_sheet.json",
                modifier = Modifier.fillMaxSize(),
                alpha = 0.18f,
                fps = 12,
                loop = true
            )
        }
        // Pass content into a nested Box so it layers on top
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}
