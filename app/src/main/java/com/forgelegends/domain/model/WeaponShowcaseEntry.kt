package com.forgelegends.domain.model

data class WeaponShowcaseEntry(
    val id: String,
    val runNumber: Int,
    val weaponFamily: WeaponFamily,
    val completedAtEpochMillis: Long,
    val totalSparks: Long = 0L
)
