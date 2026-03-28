package com.forgelegends.domain.model

data class GameState(
    val currentRunId: String = "",
    val activeConceptId: String = "",
    val sparks: Long = 0L,
    val sparksPerTap: Long = 1L,
    val forgeLevel: Int = 1,
    val currentPhase: Int = 1,
    val maxPhase: Int = 6,
    val sparksForNextPhase: Long = 100L,
    val totalSparksThisRun: Long = 0L,
    val runNumber: Int = 1,
    val permanentBonus: Double = 1.0,
    val upgrades: List<Upgrade> = defaultUpgrades()
) {
    val runCompleted: Boolean get() = currentPhase > maxPhase

    val phaseProgress: Float
        get() = if (sparksForNextPhase > 0) {
            (sparks.toFloat() / sparksForNextPhase).coerceIn(0f, 1f)
        } else 0f
}

fun defaultUpgrades(): List<Upgrade> = listOf(
    Upgrade(
        id = "furnace_temp",
        name = "Furnace Temperature",
        description = "Hotter flames, better steel",
        baseCost = 10,
        multiplier = 1.5
    ),
    Upgrade(
        id = "hammer_weight",
        name = "Hammer Weight",
        description = "Heavier strikes, faster shaping",
        baseCost = 25,
        multiplier = 2.0
    ),
    Upgrade(
        id = "metal_quality",
        name = "Metal Quality",
        description = "Purer ore, stronger weapons",
        baseCost = 50,
        multiplier = 3.0
    ),
    Upgrade(
        id = "smithing_technique",
        name = "Smithing Technique",
        description = "Ancient knowledge, masterful craft",
        baseCost = 100,
        multiplier = 5.0
    )
)
