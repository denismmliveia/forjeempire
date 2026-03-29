package com.forgelegends.ui.components.scifi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.forgelegends.ui.theme.HoloPurple
import com.forgelegends.ui.theme.NebulaSurface
import com.forgelegends.ui.theme.NeonCyan

@Composable
fun SciFiCard(
    modifier: Modifier = Modifier,
    borderStart: Color = NeonCyan,
    borderEnd: Color = HoloPurple,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .drawBehind {
                val strokeWidth = 1.5.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(borderStart, borderEnd),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = strokeWidth),
                    size = Size(size.width, size.height)
                )
            },
        shape = RoundedCornerShape(12.dp),
        color = NebulaSurface
    ) {
        Box(modifier = Modifier.padding(16.dp), content = content)
    }
}
