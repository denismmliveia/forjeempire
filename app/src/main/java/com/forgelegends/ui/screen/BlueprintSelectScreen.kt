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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.forgelegends.ui.components.scifi.GlowText
import com.forgelegends.ui.components.scifi.SciFiBackground
import com.forgelegends.ui.components.scifi.SciFiButton
import com.forgelegends.ui.components.scifi.SciFiButtonVariant
import com.forgelegends.ui.components.scifi.SciFiCard
import com.forgelegends.ui.components.scifi.SciFiProgressBar
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.NeonCyan

@Composable
fun BlueprintSelectScreen(
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

    SciFiBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlowText(
                text = "Next Blueprint",
                style = MaterialTheme.typography.headlineMedium,
                color = ElectricBlue,
                glowRadius = 14f
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress indicator
            val (completed, total) = forgeProgress
            if (total > 0) {
                Text(
                    text = "$completed / $total Projected",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                SciFiProgressBar(
                    progress = if (total > 0) completed.toFloat() / total else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (nextConcept != null) {
                NextBlueprintCard(concept = nextConcept)

                Spacer(modifier = Modifier.height(20.dp))

                SciFiButton(
                    onClick = { onForgeNext(nextConcept.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "\u2728 Project",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                // All blueprints materialized
                Text(text = "\uD83C\uDF10", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(12.dp))
                GlowText(
                    text = "All Blueprints Materialized!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = NeonCyan,
                    glowRadius = 16f,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You have projected every hologram in the queue.\nTry entering an access code to unlock hidden designs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Access Code",
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
                    label = { Text("Enter access code...") },
                    singleLine = true,
                    isError = secretError,
                    supportingText = if (secretError) {
                        { Text("Invalid code") }
                    } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submitSecretCode() }),
                    modifier = Modifier.weight(1f)
                )
                SciFiButton(
                    onClick = { submitSecretCode() },
                    variant = SciFiButtonVariant.Secondary,
                    enabled = secretCodeText.trim().isNotEmpty()
                ) {
                    Text(
                        "\uD83D\uDD13 Decode",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun NextBlueprintCard(concept: Concept) {
    val context = LocalContext.current
    val hasImages = PhaseImageProvider.hasPhaseImages(context, concept.id)

    SciFiCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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

            GlowText(
                text = concept.name,
                style = MaterialTheme.typography.headlineSmall,
                color = ElectricBlue,
                glowRadius = 12f,
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
