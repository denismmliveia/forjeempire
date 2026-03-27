package com.forgelegends.data.repository

import com.forgelegends.domain.model.GameState
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val gameState: Flow<GameState>
    suspend fun saveGameState(state: GameState)
    suspend fun clearGameState()
}
