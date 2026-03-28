package com.forgelegends.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forgelegends.ui.theme.DarkForge
import com.forgelegends.ui.theme.ForgeGold
import com.forgelegends.ui.theme.MutedText
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkForge)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onTimeout() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "\u2692\uFE0F", fontSize = 80.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Forge Legends",
                style = MaterialTheme.typography.displayLarge,
                color = ForgeGold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Legends are forged in fire",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText
            )
        }
    }
}
