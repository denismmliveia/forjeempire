package com.forgelegends.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgelegends.domain.model.Concept
import com.forgelegends.domain.model.WeaponShowcaseEntry
import com.forgelegends.ui.components.PhaseImageProvider
import com.forgelegends.ui.components.ShowcaseImageProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ModelDetailScreen(
    entry: WeaponShowcaseEntry?,
    concept: Concept?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (entry == null) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Entry not found")
            TextButton(onClick = onBack) { Text("Back") }
        }
        return
    }

    val context = LocalContext.current
    val angleCount = ShowcaseImageProvider.angleCount(context, entry.conceptId)
    val hasPhaseImages = PhaseImageProvider.hasPhaseImages(context, entry.conceptId)
    val name = concept?.name ?: entry.conceptId
    val emoji = concept?.emoji ?: "\uD83C\uDFC6"

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
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            TextButton(onClick = onBack) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (angleCount > 0) {
            MultiAngleViewer(conceptId = entry.conceptId, angleCount = angleCount)
        } else if (hasPhaseImages) {
            PhaseImageProvider.PhaseImage(
                conceptId = entry.conceptId,
                phase = 6,
                modifier = Modifier.size(280.dp)
            )
        } else {
            Text(text = emoji, fontSize = 120.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Run #${entry.runNumber}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = formatDate(entry.completedAtEpochMillis),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "${entry.totalSparks} sparks",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MultiAngleViewer(
    conceptId: String,
    angleCount: Int
) {
    var currentAngle by remember { mutableIntStateOf(0) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { dragAccumulator = 0f },
                        onDragCancel = { dragAccumulator = 0f }
                    ) { _, dragAmount ->
                        dragAccumulator += dragAmount
                        val threshold = 60f
                        if (dragAccumulator > threshold) {
                            currentAngle = (currentAngle - 1 + angleCount) % angleCount
                            dragAccumulator = 0f
                        } else if (dragAccumulator < -threshold) {
                            currentAngle = (currentAngle + 1) % angleCount
                            dragAccumulator = 0f
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentAngle,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "angle_rotation"
            ) { angle ->
                ShowcaseImageProvider.AngleImage(
                    conceptId = conceptId,
                    angle = angle,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until angleCount) {
                Box(
                    modifier = Modifier
                        .size(if (i == currentAngle) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == currentAngle) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Swipe to rotate",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
