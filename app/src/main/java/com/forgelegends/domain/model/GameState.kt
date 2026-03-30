package com.forgelegends.domain.model

data class GameState(
    val currentRunId: String = "",
    val activeConceptId: String = "",
    val sparks: Long = 0L,
    val sparksPerTap: Long = 1L,
    val sparksPerSecond: Long = 0L,
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
    // Tap upgrades
    Upgrade(
        id = "emitter_power",
        name = "Emitter Power",
        description = "Stronger emitters, faster projection",
        baseCost = 10,
        multiplier = 1.5,
        type = UpgradeType.TAP
    ),
    Upgrade(
        id = "lens_density",
        name = "Lens Density",
        description = "Denser lenses, sharper layers",
        baseCost = 25,
        multiplier = 2.0,
        type = UpgradeType.TAP
    ),
    Upgrade(
        id = "photon_purity",
        name = "Photon Purity",
        description = "Purer photons, cleaner holograms",
        baseCost = 50,
        multiplier = 3.0,
        type = UpgradeType.TAP
    ),
    Upgrade(
        id = "holo_precision",
        name = "Holo Precision",
        description = "Advanced algorithms, masterful detail",
        baseCost = 100,
        multiplier = 5.0,
        type = UpgradeType.TAP
    ),
    // Passive upgrades
    Upgrade(
        id = "auto_emitter",
        name = "Auto-Emitter",
        description = "Emits photons automatically",
        baseCost = 50,
        multiplier = 1.0,
        type = UpgradeType.PASSIVE
    ),
    Upgrade(
        id = "holo_drone",
        name = "Holo Drone",
        description = "Autonomous projection drone",
        baseCost = 200,
        multiplier = 3.0,
        type = UpgradeType.PASSIVE
    ),
    Upgrade(
        id = "quantum_loop",
        name = "Quantum Loop",
        description = "Self-sustaining photon loop",
        baseCost = 1000,
        multiplier = 10.0,
        type = UpgradeType.PASSIVE
    )
)
