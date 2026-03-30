package com.forgelegends.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgelegends.domain.model.Concept
import com.forgelegends.domain.model.WeaponShowcaseEntry
import com.forgelegends.ui.components.PhaseImageProvider
import com.forgelegends.ui.components.scifi.GlowText
import com.forgelegends.ui.components.scifi.SciFiBackground
import com.forgelegends.ui.components.scifi.SciFiCard
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.NeonCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HoloGalleryScreen(
    entries: List<WeaponShowcaseEntry>,
    conceptLookup: (String) -> Concept?,
    onEntryClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "\uD83C\uDF10 Holo Gallery",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ElectricBlue,
                    glowRadius = 14f
                )
                TextButton(onClick = onBack) {
                    Text("Back", color = NeonCyan)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (entries.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "\uD83D\uDD2E", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No holograms projected yet.\nReturn to the forge and materialize your first creation!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries.reversed()) { entry ->
                        HoloGalleryCard(
                            entry = entry,
                            concept = conceptLookup(entry.conceptId),
                            onClick = { onEntryClick(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HoloGalleryCard(
    entry: WeaponShowcaseEntry,
    concept: Concept?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val hasImages = PhaseImageProvider.hasPhaseImages(context, entry.conceptId)

    SciFiCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (hasImages) {
                PhaseImageProvider.PhaseImage(
                    conceptId = entry.conceptId,
                    phase = 6,
                    modifier = Modifier.size(80.dp)
                )
            } else {
                Text(
                    text = concept?.emoji ?: "\uD83D\uDD2E",
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = concept?.name ?: entry.conceptId,
                style = MaterialTheme.typography.bodyMedium,
                color = ElectricBlue,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Projection #${entry.runNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatGalleryDate(entry.completedAtEpochMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${entry.totalSparks} photons",
                style = MaterialTheme.typography.bodySmall,
                color = NeonCyan
            )
        }
    }
}

private fun formatGalleryDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
