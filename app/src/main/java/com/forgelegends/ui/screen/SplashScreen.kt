package com.forgelegends.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.forgelegends.ui.components.scifi.GlowText
import com.forgelegends.ui.components.scifi.LottieHoloScan
import com.forgelegends.ui.components.scifi.PhotonParticleSystem
import com.forgelegends.ui.components.scifi.SciFiBackground
import com.forgelegends.ui.theme.GhostText
import com.forgelegends.ui.theme.NeonCyan
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2500)
        onTimeout()
    }

    SciFiBackground(
        showRadialGlow = true,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onTimeout() }
    ) {
        // Particles behind everything
        PhotonParticleSystem(
            modifier = Modifier.fillMaxSize(),
            particleCount = 30
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            // Rotating holo scan behind text
            LottieHoloScan(modifier = Modifier.size(250.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(800)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GlowText(
                        text = "HOLO FORGE",
                        style = MaterialTheme.typography.displayLarge,
                        color = NeonCyan,
                        glowRadius = 20f
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GlowText(
                        text = "Materialize the impossible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GhostText,
                        glowRadius = 8f
                    )
                }
            }
        }
    }
}
