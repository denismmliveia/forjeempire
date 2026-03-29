package com.forgelegends.ui.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgelegends.domain.model.Concept
import com.forgelegends.domain.model.GameState
import com.forgelegends.ui.components.PhaseImageProvider
import com.forgelegends.ui.components.scifi.GlowText
import com.forgelegends.ui.components.scifi.LottiePhaseComplete
import com.forgelegends.ui.components.scifi.LottieTapPulse
import com.forgelegends.ui.components.scifi.PhotonParticleSystem
import com.forgelegends.ui.components.scifi.SciFiBackground
import com.forgelegends.ui.components.scifi.SciFiButton
import com.forgelegends.ui.components.scifi.SciFiButtonVariant
import com.forgelegends.ui.components.scifi.SciFiProgressBar
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.NeonCyan

@Composable
fun ForgeScreen(
    gameState: GameState,
    concept: Concept?,
    onTap: () -> Unit,
    onNavigateToWorkbench: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToCompletion: () -> Unit,
    onNavigateToShowcase: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(gameState.runCompleted) {
        if (gameState.runCompleted) {
            onNavigateToCompletion()
        }
    }

    val conceptId = gameState.activeConceptId
    val emoji = concept?.emoji ?: "\u2692\uFE0F"
    val name = concept?.name ?: conceptId

    // Tap glow intensity
    var tapGlow by remember { mutableFloatStateOf(0f) }

    // Button scale bounce
    var tapBounce by remember { mutableStateOf(false) }
    val tapScale by animateFloatAsState(
        targetValue = if (tapBounce) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "tapScale",
        finishedListener = { tapBounce = false }
    )

    // Lottie tap pulse trigger
    var tapPulsePlaying by remember { mutableStateOf(false) }

    // Phase complete animation
    var prevPhase by remember { mutableIntStateOf(gameState.currentPhase) }
    var phaseCompletePlaying by remember { mutableStateOf(false) }

    LaunchedEffect(gameState.currentPhase) {
        if (gameState.currentPhase > prevPhase) {
            phaseCompletePlaying = true
        }
        prevPhase = gameState.currentPhase
    }

    // Decay glow each frame
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameNanos { }
            tapGlow = (tapGlow - 0.02f).coerceAtLeast(0f)
            if (tapPulsePlaying && tapGlow < 0.01f) {
                tapPulsePlaying = false
            }
        }
    }

    val handleTap = {
        tapGlow = (tapGlow + 0.18f).coerceAtMost(1f)
        tapBounce = true
        tapPulsePlaying = true
        onTap()
    }

    SciFiBackground(showRadialGlow = true) {
        // Ambient particle layer behind everything
        PhotonParticleSystem(
            modifier = Modifier.fillMaxSize(),
            particleCount = 20
        )

        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val context = LocalContext.current
                val hasImages = PhaseImageProvider.hasPhaseImages(context, conceptId)

                // Model image with phase complete overlay
                Box(contentAlignment = Alignment.Center) {
                    if (hasImages) {
                        PhaseImageProvider.PhaseImage(
                            conceptId = conceptId,
                            phase = gameState.currentPhase,
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        Text(text = emoji, fontSize = 72.sp)
                    }

                    // Phase complete burst overlay
                    if (phaseCompletePlaying) {
                        LottiePhaseComplete(
                            isPlaying = true,
                            modifier = Modifier.size(250.dp)
                        )
                        LaunchedEffect(phaseCompletePlaying) {
                            kotlinx.coroutines.delay(1000)
                            phaseCompletePlaying = false
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                GlowText(
                    text = "$name — Layer ${gameState.currentPhase}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ElectricBlue,
                    glowRadius = 14f
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Projection #${gameState.runNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlowText(
                    text = "${gameState.sparks} / ${gameState.sparksForNextPhase} photons",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    glowRadius = 10f
                )

                Text(
                    text = "+${gameState.sparksPerTap} per tap" +
                            if (gameState.sparksPerSecond > 0) " | +${gameState.sparksPerSecond}/sec" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                SciFiProgressBar(
                    progress = gameState.phaseProgress,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Tap button with dynamic glow + scale bounce + Lottie pulse
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(190.dp)
                        .graphicsLayer(
                            scaleX = tapScale,
                            scaleY = tapScale
                        )
                        .drawBehind {
                            if (tapGlow > 0.01f) {
                                // Outer ring glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            NeonCyan.copy(alpha = tapGlow * 0.15f),
                                            ElectricBlue.copy(alpha = tapGlow * 0.5f),
                                            NeonCyan.copy(alpha = tapGlow * 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension / 2 + (tapGlow * 50.dp.toPx())
                                )
                                // Inner bright core
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = tapGlow * 0.2f),
                                            ElectricBlue.copy(alpha = tapGlow * 0.4f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension / 2 * 0.9f
                                )
                            }
                        }
                ) {
                    // Lottie ring pulse behind button
                    LottieTapPulse(
                        isPlaying = tapPulsePlaying,
                        modifier = Modifier.size(190.dp)
                    )

                    SciFiButton(
                        onClick = handleTap,
                        modifier = Modifier.size(150.dp)
                    ) {
                        Text(
                            text = "\u26A1",
                            fontSize = 48.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SciFiButton(
                    onClick = onNavigateToWorkbench,
                    variant = SciFiButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "\uD83D\uDD2C Holo Lab",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                SciFiButton(
                    onClick = onNavigateToProgress,
                    variant = SciFiButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "\uD83D\uDCA0 Layers",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Floating Holo Gallery button on the right side
            SciFiButton(
                onClick = onNavigateToShowcase,
                variant = SciFiButtonVariant.Outlined,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("\uD83C\uDF10", fontSize = 18.sp)
                    Text(
                        "Gallery",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}
