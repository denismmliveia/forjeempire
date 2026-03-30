package com.forgelegends.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.forgelegends.ui.components.scifi.AnimatedSpriteSheet
import com.forgelegends.ui.components.scifi.GlowText
import com.forgelegends.ui.components.scifi.LottieHoloScan
import com.forgelegends.ui.components.scifi.PhotonParticleSystem
import com.forgelegends.ui.components.scifi.SciFiBackground
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.GhostText
import com.forgelegends.ui.theme.NeonCyan
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    onEnter: () -> Unit = {}
) {
    val context = LocalContext.current

    var logoVisible by remember { mutableStateOf(false) }
    var transitioning by remember { mutableStateOf(false) }
    var flashAlpha by remember { mutableStateOf(0f) }

    // Load logo bitmap from drawable or assets
    val logoBitmap = remember {
        try {
            // Try assets first (user can place holo_forge_logo.png in assets/)
            context.assets.open("holo_forge_logo.png").use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            try {
                // Fallback: try drawable by resource name
                val resId = context.resources.getIdentifier("holo_forge_logo", "drawable", context.packageName)
                if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) else null
            } catch (_: Exception) { null }
        }
    }

    val flashAnim by animateFloatAsState(
        targetValue = flashAlpha,
        animationSpec = tween(if (flashAlpha > 0f) 80 else 400),
        label = "splash_flash"
    )

    val doTransition = {
        if (!transitioning) {
            transitioning = true
            flashAlpha = 1f
        }
    }

    LaunchedEffect(Unit) {
        onEnter()
        delay(200)
        logoVisible = true
        delay(2800)
        doTransition()
    }

    LaunchedEffect(transitioning) {
        if (transitioning) {
            delay(100) // slight delay then start fading flash
            flashAlpha = 0f
            delay(750) // let energy burst animation play
            onTimeout()
        }
    }

    SciFiBackground(
        showRadialGlow = true,
        showAnimatedTexture = true,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { doTransition() }
    ) {
        // Particles behind everything
        PhotonParticleSystem(
            modifier = Modifier.fillMaxSize(),
            particleCount = 35
        )

        // Main content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(animationSpec = tween(900))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Logo image or fallback holo scan
                    Box(contentAlignment = Alignment.Center) {
                        if (logoBitmap != null) {
                            // Holo scan ring behind logo
                            LottieHoloScan(modifier = Modifier.size(300.dp))
                            Image(
                                bitmap = logoBitmap.asImageBitmap(),
                                contentDescription = "Holo Forge",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(200.dp)
                            )
                        } else {
                            // Fallback: animated holo scan with text logo
                            LottieHoloScan(modifier = Modifier.size(250.dp))
                            GlowText(
                                text = "HF",
                                style = MaterialTheme.typography.displayLarge,
                                color = NeonCyan,
                                glowRadius = 28f
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    GlowText(
                        text = "HOLO FORGE",
                        style = MaterialTheme.typography.displayLarge,
                        color = NeonCyan,
                        glowRadius = 20f
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GlowText(
                        text = "Materialize the impossible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GhostText,
                        glowRadius = 8f
                    )
                }
            }

            // Energy burst full-screen transition overlay
            AnimatedVisibility(
                visible = transitioning,
                enter = fadeIn(animationSpec = tween(50)),
                exit = fadeOut(animationSpec = tween(600))
            ) {
                AnimatedSpriteSheet(
                    sheetAssetPath = "textures/energy_burst_sheet.png",
                    metadataAssetPath = "textures/energy_burst_sheet.json",
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.85f,
                    fps = 22,
                    loop = false,
                    playing = true
                )
            }

            // Radial flash on transition
            if (flashAnim > 0.01f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = flashAnim * 0.5f),
                                NeonCyan.copy(alpha = flashAnim * 0.35f),
                                ElectricBlue.copy(alpha = flashAnim * 0.15f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.maxDimension * 0.8f
                        )
                    )
                }
            }
        }
    }
}
