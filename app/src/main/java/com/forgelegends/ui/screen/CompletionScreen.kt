package com.forgelegends.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.forgelegends.domain.model.Concept
import com.forgelegends.domain.model.GameState
import com.forgelegends.ui.components.scifi.GlowText
import com.forgelegends.ui.components.scifi.SciFiBackground
import com.forgelegends.ui.components.scifi.SciFiButton
import com.forgelegends.ui.components.scifi.SciFiButtonVariant
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.NeonCyan

@Composable
fun CompletionScreen(
    gameState: GameState,
    concept: Concept?,
    onNavigateToConceptSelect: () -> Unit,
    onNavigateToShowcase: () -> Unit,
    modifier: Modifier = Modifier
) {
    SciFiBackground(showRadialGlow = true) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlowText(
                text = "Hologram Materialized!",
                style = MaterialTheme.typography.headlineMedium,
                color = ElectricBlue,
                glowRadius = 20f,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            GlowText(
                text = concept?.name ?: gameState.activeConceptId,
                style = MaterialTheme.typography.titleLarge,
                color = NeonCyan,
                glowRadius = 14f,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total photons: ${gameState.totalSparksThisRun}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            SciFiButton(
                onClick = onNavigateToConceptSelect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "\u2728 New Projection",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
