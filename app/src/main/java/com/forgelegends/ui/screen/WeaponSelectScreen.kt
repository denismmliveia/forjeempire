package com.forgelegends.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgelegends.domain.model.WeaponFamily
import com.forgelegends.domain.model.WeaponShowcaseEntry
import com.forgelegends.ui.components.PhaseImageProvider
import com.forgelegends.ui.components.WeaponVisualRegistry

@Composable
fun WeaponSelectScreen(
    showcaseEntries: List<WeaponShowcaseEntry>,
    onSelectWeapon: (WeaponFamily) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Your Next Forge",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select a weapon to craft",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        val forgedFamilies = showcaseEntries.map { it.weaponFamily }.toSet()

        WeaponFamily.entries.forEach { family ->
            WeaponFamilyCard(
                family = family,
                forged = family in forgedFamilies,
                onClick = { onSelectWeapon(family) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WeaponFamilyCard(
    family: WeaponFamily,
    forged: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val hasImages = PhaseImageProvider.hasPhaseImages(context, family)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasImages) {
                PhaseImageProvider.PhaseImage(
                    family = family,
                    phase = 6,
                    modifier = Modifier.size(100.dp)
                )
            } else {
                Text(
                    text = WeaponVisualRegistry.phaseEmoji(family, 6),
                    fontSize = 56.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = WeaponVisualRegistry.victoryLabel(family),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            if (forged) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\u2705 Previously Forged",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
