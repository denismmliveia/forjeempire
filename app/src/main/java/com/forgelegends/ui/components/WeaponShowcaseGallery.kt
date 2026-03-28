package com.forgelegends.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgelegends.domain.model.Concept
import com.forgelegends.domain.model.WeaponShowcaseEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeaponShowcaseGallery(
    entries: List<WeaponShowcaseEntry>,
    conceptLookup: (String) -> Concept?,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        Text(
            text = "No legendary creations forged yet...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(32.dp)
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(entries.reversed()) { entry ->
            ShowcaseCard(entry, conceptLookup)
        }
    }
}

@Composable
private fun ShowcaseCard(
    entry: WeaponShowcaseEntry,
    conceptLookup: (String) -> Concept?
) {
    val concept = conceptLookup(entry.conceptId)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = concept?.name ?: entry.conceptId,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Run #${entry.runNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(entry.completedAtEpochMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = concept?.emoji ?: "\uD83C\uDFC6",
                style = MaterialTheme.typography.displayLarge
            )
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
