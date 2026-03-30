package com.forgelegends.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.forgelegends.domain.model.Concept
import com.forgelegends.domain.model.GameState
import com.forgelegends.ui.components.PhaseImageProvider
import com.forgelegends.ui.components.scifi.GlowText
import com.forgelegends.ui.components.scifi.LottiePhaseComplete
import com.forgelegends.ui.components.scifi.LottieTapPulse
import com.forgelegends.ui.components.scifi.PhotonParticleSystem
import com.forgelegends.ui.components.scifi.SciFiBackground
import com.forgelegends.ui.components.scifi.HoloModelFrame
import com.forgelegends.ui.components.scifi.HoloNavButton
import com.forgelegends.ui.components.scifi.HoloTapButton
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

    // Slow underlight pulse below the tap button
    val underlightTransition = rememberInfiniteTransition(label = "underlight")
    val underlightAlpha by underlightTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "underlightAlpha"
    )

    SciFiBackground(showRadialGlow = true, showAnimatedTexture = true) {
        // Ambient particle layer behind everything
        PhotonParticleSystem(
            modifier = Modifier.fillMaxSize(),
            particleCount = 20
        )

        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                val context = LocalContext.current
                val hasImages = PhaseImageProvider.hasPhaseImages(context, conceptId)

                Spacer(modifier = Modifier.weight(0.6f))

                // Model image inside holographic frame
                Box(contentAlignment = Alignment.Center) {
                    if (hasImages) {
                        HoloModelFrame(modifier = Modifier.size(200.dp)) {
                            PhaseImageProvider.PhaseImage(
                                conceptId = conceptId,
                                phase = gameState.currentPhase,
                                modifier = Modifier.size(174.dp)
                            )
                        }
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

                Spacer(modifier = Modifier.height(6.dp))

                GlowText(
                    text = "$name — Layer ${gameState.currentPhase}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ElectricBlue,
                    glowRadius = 14f
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Projection #${gameState.runNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                SciFiProgressBar(
                    progress = gameState.phaseProgress,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                // Tap button with underlight glow + holo ring design
                Box(contentAlignment = Alignment.Center) {
                    // Slow radial underlight from below the button
                    Canvas(modifier = Modifier.size(260.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height * 0.72f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00AAFF).copy(alpha = underlightAlpha),
                                    Color(0xFF0055CC).copy(alpha = underlightAlpha * 0.35f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = size.width * 0.44f
                            ),
                            radius = size.width * 0.44f,
                            center = Offset(cx, cy)
                        )
                    }

                    // Lottie ring pulse behind button
                    LottieTapPulse(
                        isPlaying = tapPulsePlaying,
                        modifier = Modifier.size(210.dp)
                    )

                    HoloTapButton(
                        onClick = handleTap,
                        tapGlow = tapGlow,
                        tapScale = tapScale,
                        modifier = Modifier.size(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                HoloNavButton(
                    text = "\uD83D\uDD2C  Holo Lab",
                    onClick = onNavigateToWorkbench,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                HoloNavButton(
                    text = "\uD83D\uDCA0  Layers",
                    onClick = onNavigateToProgress,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(0.3f))
            }

            // Floating Gallery tab — flush against the right screen edge
            val galleryTabShape = RoundedCornerShape(
                topStart = 18.dp, bottomStart = 18.dp,
                topEnd = 0.dp, bottomEnd = 0.dp
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    // Outer glow behind the tab
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.25f),
                                    NeonCyan.copy(alpha = 0f)
                                ),
                                center = Offset(0f, size.height / 2f),
                                radius = size.width * 1.4f
                            )
                        )
                    }
                    .clip(galleryTabShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                ElectricBlue.copy(alpha = 0.55f),
                                ElectricBlue.copy(alpha = 0.20f)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.9f), NeonCyan.copy(alpha = 0.3f))
                        ),
                        shape = galleryTabShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNavigateToShowcase
                    )
                    .padding(start = 10.dp, end = 6.dp, top = 18.dp, bottom = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("\uD83C\uDF10", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(4.dp))
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
