package com.forgelegends.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgelegends.domain.model.GameState
import com.forgelegends.domain.model.UpgradeType
import com.forgelegends.ui.components.scifi.GlowText
import com.forgelegends.ui.components.scifi.SciFiBackground
import com.forgelegends.ui.components.scifi.SciFiButton
import com.forgelegends.ui.components.scifi.SciFiCard
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.GhostText
import com.forgelegends.ui.theme.HoloPurple
import com.forgelegends.ui.theme.NeonCyan
import com.forgelegends.ui.theme.NeonGreen

@Composable
fun HoloLabScreen(
    gameState: GameState,
    onPurchaseUpgrade: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tapUpgrades = gameState.upgrades.filter { it.type == UpgradeType.TAP }
    val passiveUpgrades = gameState.upgrades.filter { it.type == UpgradeType.PASSIVE }

    SciFiBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlowText(
                    text = "Holo Lab",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ElectricBlue,
                    glowRadius = 14f
                )
                TextButton(onClick = onBack) {
                    Text("Back", color = NeonCyan)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Currency display
            GlowText(
                text = "Photons: ${gameState.sparks}",
                style = MaterialTheme.typography.titleLarge,
                color = NeonCyan,
                glowRadius = 10f
            )

            if (gameState.sparksPerSecond > 0) {
                GlowText(
                    text = "+${gameState.sparksPerSecond}/sec",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonGreen,
                    glowRadius = 6f
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tap upgrades section
                item {
                    Text(
                        text = "\u26A1 Tap Upgrades",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricBlue
                    )
                }

                items(tapUpgrades) { upgrade ->
                    UpgradeCard(
                        name = upgrade.name,
                        description = upgrade.description,
                        level = upgrade.level,
                        cost = upgrade.currentCost,
                        canAfford = gameState.sparks >= upgrade.currentCost,
                        onPurchase = { onPurchaseUpgrade(upgrade.id) }
                    )
                }

                // Passive upgrades section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\uD83D\uDD04 Passive Upgrades",
                        style = MaterialTheme.typography.titleMedium,
                        color = HoloPurple
                    )
                    Text(
                        text = "Generate photons automatically every second",
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostText
                    )
                }

                items(passiveUpgrades) { upgrade ->
                    UpgradeCard(
                        name = upgrade.name,
                        description = upgrade.description,
                        level = upgrade.level,
                        cost = upgrade.currentCost,
                        canAfford = gameState.sparks >= upgrade.currentCost,
                        onPurchase = { onPurchaseUpgrade(upgrade.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    name: String,
    description: String,
    level: Int,
    cost: Long,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    SciFiCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$name (Lv.$level)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SciFiButton(
                onClick = onPurchase,
                enabled = canAfford
            ) {
                Text(
                    "$cost",
                    color = if (canAfford) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
