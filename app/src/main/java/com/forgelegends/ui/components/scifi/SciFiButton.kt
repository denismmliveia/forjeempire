package com.forgelegends.ui.components.scifi

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.forgelegends.ui.theme.CyanGlow
import com.forgelegends.ui.theme.DeepSpace
import com.forgelegends.ui.theme.ElectricBlue
import com.forgelegends.ui.theme.HoloPurple
import com.forgelegends.ui.theme.NeonCyan

enum class SciFiButtonVariant { Primary, Secondary, Outlined }

@Composable
fun SciFiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SciFiButtonVariant = SciFiButtonVariant.Primary,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 0.3f,
        label = "glow"
    )

    val shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp)

    val backgroundModifier = when (variant) {
        SciFiButtonVariant.Primary -> Modifier.background(
            brush = Brush.linearGradient(listOf(NeonCyan, ElectricBlue)),
            shape = shape
        )
        SciFiButtonVariant.Secondary -> Modifier.background(
            brush = Brush.linearGradient(listOf(HoloPurple.copy(alpha = 0.6f), ElectricBlue.copy(alpha = 0.4f))),
            shape = shape
        )
        SciFiButtonVariant.Outlined -> Modifier
            .background(Color.Transparent, shape)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(NeonCyan, HoloPurple)),
                shape = shape
            )
    }

    val glowModifier = if (variant != SciFiButtonVariant.Outlined) {
        Modifier.drawBehind {
            drawRoundRect(
                color = CyanGlow.copy(alpha = glowAlpha),
                cornerRadius = CornerRadius(12.dp.toPx())
            )
        }
    } else Modifier

    Box(
        modifier = modifier
            .then(glowModifier)
            .then(backgroundModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
