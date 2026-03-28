package com.forgelegends.data.repository

import kotlinx.coroutines.flow.Flow

interface PlayerProgressRepository {
    val playerSeed: Flow<Long>
    val queuePosition: Flow<Int>
    val unlockedSecretIds: Flow<Set<String>>
    suspend fun initSeedIfAbsent()
    suspend fun advanceQueue()
    suspend fun addUnlockedSecret(conceptId: String)
}
