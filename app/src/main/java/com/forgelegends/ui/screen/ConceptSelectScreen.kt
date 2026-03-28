package com.forgelegends.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.LinearProgressIndicator
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
    nextConcept: Concept?,
    forgeProgress: Pair<Int, Int>,
    showcaseEntries: List<WeaponShowcaseEntry>,
    onForgeNext: (String) -> Unit,
    onTrySecretCode: (String) -> Concept?,
    onForgeSecret: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var secretCodeText by remember { mutableStateOf("") }
    var secretError by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun submitSecretCode() {
        val trimmed = secretCodeText.trim()
        if (trimmed.isEmpty()) return
        val concept = onTrySecretCode(trimmed)
        if (concept != null) {
            secretError = false
            secretCodeText = ""
            keyboardController?.hide()
            onForgeSecret(concept.id)
        } else {
            secretError = true
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
            text = "Your Next Forge",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress indicator
        val (completed, total) = forgeProgress
        if (total > 0) {
            Text(
                text = "$completed / $total Forged",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total > 0) completed.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (nextConcept != null) {
            // Next concept card
            NextConceptCard(concept = nextConcept)

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onForgeNext(nextConcept.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("\u2692\uFE0F Forge", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            // All concepts forged
            Text(
                text = "\uD83C\uDFC6",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "All Concepts Forged!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You have mastered every forge in the queue.\nTry entering a secret code to unlock hidden designs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Secret Code",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = secretCodeText,
                onValueChange = {
                    secretCodeText = it
                    secretError = false
                },
                label = { Text("Enter secret code...") },
                singleLine = true,
                isError = secretError,
                supportingText = if (secretError) {
                    { Text("Invalid code") }
                } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submitSecretCode() }),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { submitSecretCode() },
                enabled = secretCodeText.trim().isNotEmpty()
            ) {
                Text("\uD83D\uDD13 Unlock")
            }
        }
    }
}

@Composable
private fun NextConceptCard(concept: Concept) {
    val context = LocalContext.current
    val hasImages = PhaseImageProvider.hasPhaseImages(context, concept.id)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasImages) {
                PhaseImageProvider.PhaseImage(
                    conceptId = concept.id,
                    phase = 6,
                    modifier = Modifier.size(140.dp)
                )
            } else {
                Text(text = concept.emoji, fontSize = 72.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = concept.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            if (concept.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = concept.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
