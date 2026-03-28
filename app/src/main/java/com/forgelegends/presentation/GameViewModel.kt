package com.forgelegends.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgelegends.data.repository.CustomConceptRepository
import com.forgelegends.data.repository.GameRepository
import com.forgelegends.data.repository.WeaponShowcaseRepository
import com.forgelegends.domain.model.Concept
import com.forgelegends.domain.model.GameState
import com.forgelegends.domain.model.WeaponShowcaseEntry
import com.forgelegends.domain.model.defaultUpgrades
import com.forgelegends.domain.registry.ConceptRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val showcaseRepository: WeaponShowcaseRepository,
    private val customConceptRepository: CustomConceptRepository,
    val conceptRegistry: ConceptRegistry
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _customConcepts = MutableStateFlow<List<Concept>>(emptyList())

    val allConcepts: StateFlow<List<Concept>> = kotlinx.coroutines.flow.combine(
        kotlinx.coroutines.flow.flowOf(conceptRegistry.concepts),
        _customConcepts
    ) { assets, custom ->
        assets + custom.filter { c -> assets.none { it.id == c.id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), conceptRegistry.concepts)

    // Keep backward-compat property
    val concepts: List<Concept> get() = allConcepts.value

    val showcaseEntries: StateFlow<List<WeaponShowcaseEntry>> =
        showcaseRepository.entries.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val archivedRunIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            gameRepository.gameState.collect { saved ->
                val state = if (saved.currentRunId.isEmpty()) {
                    saved.copy(currentRunId = UUID.randomUUID().toString())
                } else {
                    saved
                }
                _gameState.value = state
            }
        }
        viewModelScope.launch {
            customConceptRepository.concepts.collect { list ->
                _customConcepts.value = list
            }
        }
    }

    fun addCustomConcept(name: String) {
        val id = name.trim().lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(40)
        val concept = Concept(
            id = id,
            name = name.trim(),
            emoji = "\u2692\uFE0F",
            description = ""
        )
        viewModelScope.launch {
            customConceptRepository.addConcept(concept)
        }
    }

    fun getActiveConcept(): Concept? =
        conceptRegistry.getById(_gameState.value.activeConceptId)

    fun onTap() {
        val current = _gameState.value
        if (current.runCompleted) return

        val earned = (current.sparksPerTap * current.permanentBonus).toLong()
        val newSparks = current.sparks + earned
        val newTotal = current.totalSparksThisRun + earned

        val updated = if (newSparks >= current.sparksForNextPhase) {
            val nextPhase = current.currentPhase + 1
            current.copy(
                sparks = 0L,
                totalSparksThisRun = newTotal,
                currentPhase = nextPhase,
                sparksForNextPhase = calculateSparksForPhase(nextPhase)
            )
        } else {
            current.copy(sparks = newSparks, totalSparksThisRun = newTotal)
        }

        _gameState.value = updated
        persistState(updated)
    }

    fun purchaseUpgrade(upgradeId: String) {
        val current = _gameState.value
        val idx = current.upgrades.indexOfFirst { it.id == upgradeId }
        if (idx == -1) return

        val upgrade = current.upgrades[idx]
        if (current.sparks < upgrade.currentCost) return

        val newUpgrades = current.upgrades.toMutableList()
        newUpgrades[idx] = upgrade.copy(level = upgrade.level + 1)

        val newSparksPerTap = calculateSparksPerTap(newUpgrades)
        val updated = current.copy(
            sparks = current.sparks - upgrade.currentCost,
            upgrades = newUpgrades,
            sparksPerTap = newSparksPerTap
        )

        _gameState.value = updated
        persistState(updated)
    }

    fun archiveCurrentRun() {
        val current = _gameState.value
        if (!current.runCompleted) return
        if (current.currentRunId in archivedRunIds) return

        archivedRunIds.add(current.currentRunId)
        val entry = WeaponShowcaseEntry(
            id = current.currentRunId,
            runNumber = current.runNumber,
            conceptId = current.activeConceptId,
            completedAtEpochMillis = System.currentTimeMillis(),
            totalSparks = current.totalSparksThisRun
        )

        viewModelScope.launch {
            showcaseRepository.addIfAbsent(entry)
        }
    }

    fun archiveAndStartNewRun(conceptId: String) {
        val current = _gameState.value
        if (!current.runCompleted) return

        archiveCurrentRun()

        val newState = GameState(
            currentRunId = UUID.randomUUID().toString(),
            activeConceptId = conceptId,
            runNumber = current.runNumber + 1,
            permanentBonus = current.permanentBonus + 0.1,
            upgrades = defaultUpgrades()
        )

        _gameState.value = newState
        persistState(newState)
    }

    fun startNewRun(conceptId: String) {
        val current = _gameState.value

        val newState = GameState(
            currentRunId = UUID.randomUUID().toString(),
            activeConceptId = conceptId,
            runNumber = if (current.runCompleted || current.activeConceptId.isEmpty()) current.runNumber + 1 else 1,
            permanentBonus = if (current.runCompleted) current.permanentBonus + 0.1 else 1.0,
            upgrades = defaultUpgrades()
        )

        _gameState.value = newState
        persistState(newState)
    }

    private fun persistState(state: GameState) {
        viewModelScope.launch {
            gameRepository.saveGameState(state)
        }
    }

    private fun calculateSparksForPhase(phase: Int): Long {
        return (100 * Math.pow(2.5, (phase - 1).toDouble())).toLong()
    }

    private fun calculateSparksPerTap(upgrades: List<com.forgelegends.domain.model.Upgrade>): Long {
        var base = 1.0
        for (u in upgrades) {
            if (u.level > 0) {
                base += u.multiplier * u.level
            }
        }
        return base.toLong().coerceAtLeast(1L)
    }
}
