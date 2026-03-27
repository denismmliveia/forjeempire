package com.forgelegends.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgelegends.domain.model.GameState
import com.forgelegends.ui.components.WeaponVisualRegistry

@Composable
fun ForgeScreen(
    gameState: GameState,
    onTap: () -> Unit,
    onNavigateToWorkbench: () -> Unit,
    onNavigateToProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = WeaponVisualRegistry.phaseEmoji(gameState.activeWeaponFamily, gameState.currentPhase),
            fontSize = 72.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = WeaponVisualRegistry.phaseLabel(gameState.activeWeaponFamily, gameState.currentPhase),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Run #${gameState.runNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "${gameState.sparks} / ${gameState.sparksForNextPhase} sparks",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "+${gameState.sparksPerTap} per tap",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onTap,
            modifier = Modifier.size(160.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "\uD83D\uDD28",
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onNavigateToWorkbench) {
            Text("Workbench")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onNavigateToProgress) {
            Text("Weapon Progress")
        }
    }
}
