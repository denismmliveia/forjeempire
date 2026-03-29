package com.forgelegends.ui.components.scifi

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.forgelegends.ui.theme.NeonCyan

@Composable
fun GlowText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    color: Color = NeonCyan,
    glowColor: Color = color.copy(alpha = 0.6f),
    glowRadius: Float = 12f,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = color,
            shadow = Shadow(
                color = glowColor,
                offset = Offset.Zero,
                blurRadius = glowRadius
            )
        ),
        textAlign = textAlign
    )
}
