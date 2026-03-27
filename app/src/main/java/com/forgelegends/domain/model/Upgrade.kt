package com.forgelegends.domain.model

data class Upgrade(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: Long,
    val multiplier: Double,
    val level: Int = 0
) {
    val currentCost: Long get() = (baseCost * Math.pow(1.15, level.toDouble())).toLong()
}
