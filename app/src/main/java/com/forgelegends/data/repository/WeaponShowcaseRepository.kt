package com.forgelegends.data.repository

import com.forgelegends.domain.model.WeaponShowcaseEntry
import kotlinx.coroutines.flow.Flow

interface WeaponShowcaseRepository {
    val entries: Flow<List<WeaponShowcaseEntry>>
    suspend fun addIfAbsent(entry: WeaponShowcaseEntry)
}
