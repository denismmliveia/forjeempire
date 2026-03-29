package com.forgelegends.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgelegends.domain.model.Concept
import com.forgelegends.domain.model.GameState
import com.forgelegends.ui.components.PhaseImageProvider
import com.forgelegends.ui.components.scifi.GlowText
import com.forgelegends.ui.components.scifi.PhaseState
import com.forgelegends.ui.components.scifi.SciFiBackground
import com.forgelegends.ui.components.scifi.SciFiButton
import com.forgelegends.ui.components.scifi.SciFiPhaseIndicator
import com.forgelegends.ui.components.scifi.SciFiProgressBar
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.NeonCyan

@Composable
fun LayerProgressScreen(
    gameState: GameState,
    concept: Concept?,
    onBack: () -> Unit,
    onNavigateToCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conceptId = gameState.activeConceptId

    SciFiBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlowText(
                    text = "Holo Layers",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ElectricBlue,
                    glowRadius = 14f
                )
                TextButton(onClick = onBack) {
                    Text("Back", color = NeonCyan)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val context = LocalContext.current
            val hasImages = PhaseImageProvider.hasPhaseImages(context, conceptId)

            for (phase in 1..gameState.maxPhase) {
                val completed = phase < gameState.currentPhase
                val current = phase == gameState.currentPhase
                val phaseState = when {
                    completed -> PhaseState.Completed
                    current -> PhaseState.Active
                    else -> PhaseState.Locked
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (hasImages) {
                        PhaseImageProvider.PhaseImage(
                            conceptId = conceptId,
                            phase = phase,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        val emoji = concept?.emoji ?: "\u2692\uFE0F"
                        Text(text = emoji, fontSize = 28.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Layer $phase",
                            style = MaterialTheme.typography.bodyLarge,
                            color = when {
                                completed -> ElectricBlue
                                current -> NeonCyan
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        if (current) {
                            Spacer(modifier = Modifier.height(4.dp))
                            SciFiProgressBar(
                                progress = gameState.phaseProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                        }
                    }

                    SciFiPhaseIndicator(
                        state = phaseState,
                        size = 32.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Total photons: ${gameState.totalSparksThisRun}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (gameState.runCompleted) {
                Spacer(modifier = Modifier.height(24.dp))
                SciFiButton(
                    onClick = onNavigateToCompletion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "View Hologram!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
