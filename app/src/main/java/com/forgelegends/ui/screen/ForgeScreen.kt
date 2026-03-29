package com.forgelegends.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgelegends.domain.model.Concept
import com.forgelegends.domain.model.GameState
import com.forgelegends.ui.components.PhaseImageProvider
import com.forgelegends.ui.components.scifi.GlowText
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

    SciFiBackground(showRadialGlow = true) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current
            val hasImages = PhaseImageProvider.hasPhaseImages(context, conceptId)

            if (hasImages) {
                PhaseImageProvider.PhaseImage(
                    conceptId = conceptId,
                    phase = gameState.currentPhase,
                    modifier = Modifier.size(200.dp)
                )
            } else {
                Text(text = emoji, fontSize = 72.sp)
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
                text = "+${gameState.sparksPerTap} per tap",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            SciFiProgressBar(
                progress = gameState.phaseProgress,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            SciFiButton(
                onClick = onTap,
                modifier = Modifier.size(160.dp)
            ) {
                Text(
                    text = "\u26A1",
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center
                )
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

            Spacer(modifier = Modifier.height(8.dp))

            SciFiButton(
                onClick = onNavigateToShowcase,
                variant = SciFiButtonVariant.Outlined,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "\uD83C\uDF10 Holo Gallery",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan
                )
            }
        }
    }
}
