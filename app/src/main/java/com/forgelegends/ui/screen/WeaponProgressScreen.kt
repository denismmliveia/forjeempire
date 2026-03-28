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
import androidx.compose.material3.LinearProgressIndicator
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

@Composable
fun WeaponProgressScreen(
    gameState: GameState,
    concept: Concept?,
    onBack: () -> Unit,
    onNavigateToCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conceptId = gameState.activeConceptId
    val emoji = concept?.emoji ?: "\u2692\uFE0F"

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
            Text(
                text = concept?.name ?: "Progress",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val context = LocalContext.current
        val hasImages = PhaseImageProvider.hasPhaseImages(context, conceptId)

        for (phase in 1..gameState.maxPhase) {
            val completed = phase < gameState.currentPhase
            val current = phase == gameState.currentPhase
            val label = "Phase $phase"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
                    Text(text = emoji, fontSize = 28.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            completed -> MaterialTheme.colorScheme.secondary
                            current -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    if (current) {
                        LinearProgressIndicator(
                            progress = { gameState.phaseProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
                Text(
                    text = if (completed) "\u2705" else if (current) "\uD83D\uDD25" else "\u2B1C",
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Total sparks: ${gameState.totalSparksThisRun}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (gameState.runCompleted) {
            Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.Button(onClick = onNavigateToCompletion) {
                Text("View Completed Creation!")
            }
        }
    }
}
