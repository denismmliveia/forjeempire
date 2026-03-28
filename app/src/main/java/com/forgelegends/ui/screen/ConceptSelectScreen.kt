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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgelegends.domain.model.Concept
import com.forgelegends.domain.model.WeaponShowcaseEntry
import com.forgelegends.ui.components.PhaseImageProvider

@Composable
fun ConceptSelectScreen(
    concepts: List<Concept>,
    showcaseEntries: List<WeaponShowcaseEntry>,
    onSelectConcept: (String) -> Unit,
    onAddCustomConcept: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newConceptText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun submitNewConcept() {
        val trimmed = newConceptText.trim()
        if (trimmed.isNotEmpty()) {
            onAddCustomConcept(trimmed)
            newConceptText = ""
            keyboardController?.hide()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Your Next Forge",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter any concept to forge",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // New concept input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newConceptText,
                onValueChange = { newConceptText = it },
                label = { Text("New concept...") },
                placeholder = { Text("e.g. a dragon, a robot, a ninja...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submitNewConcept() }),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { submitNewConcept() },
                enabled = newConceptText.trim().isNotEmpty()
            ) {
                Text("\u2692\uFE0F Forge")
            }
        }

        if (concepts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Or pick an existing concept",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val forgedIds = showcaseEntries.map { it.conceptId }.toSet()

            concepts.forEach { concept ->
                ConceptCard(
                    concept = concept,
                    forged = concept.id in forgedIds,
                    onClick = { onSelectConcept(concept.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ConceptCard(
    concept: Concept,
    forged: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val hasImages = PhaseImageProvider.hasPhaseImages(context, concept.id)

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
                    conceptId = concept.id,
                    phase = 6,
                    modifier = Modifier.size(100.dp)
                )
            } else {
                Text(text = concept.emoji, fontSize = 56.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = concept.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            if (concept.description.isNotEmpty()) {
                Text(
                    text = concept.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

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
