package com.forgelegends.domain.model

enum class UpgradeType { TAP, PASSIVE }

data class Upgrade(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: Long,
    val multiplier: Double,
    val level: Int = 0,
    val type: UpgradeType = UpgradeType.TAP
) {
    val currentCost: Long get() = (baseCost * Math.pow(1.15, level.toDouble())).toLong()
}
