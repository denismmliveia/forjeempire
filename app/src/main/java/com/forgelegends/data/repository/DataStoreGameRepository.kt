package com.forgelegends.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.forgelegends.domain.model.GameState
import com.forgelegends.domain.model.WeaponFamily
import com.forgelegends.domain.model.defaultUpgrades
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DataStoreGameRepository @Inject constructor(
    @Named("game_state") private val dataStore: DataStore<Preferences>
) : GameRepository {

    private companion object {
        val KEY_RUN_ID = stringPreferencesKey("current_run_id")
        val KEY_WEAPON_FAMILY = stringPreferencesKey("active_weapon_family")
        val KEY_SPARKS = longPreferencesKey("sparks")
        val KEY_SPARKS_PER_TAP = longPreferencesKey("sparks_per_tap")
        val KEY_FORGE_LEVEL = intPreferencesKey("forge_level")
        val KEY_CURRENT_PHASE = intPreferencesKey("current_phase")
        val KEY_SPARKS_FOR_NEXT = longPreferencesKey("sparks_for_next_phase")
        val KEY_TOTAL_SPARKS = longPreferencesKey("total_sparks_this_run")
        val KEY_RUN_NUMBER = intPreferencesKey("run_number")
        val KEY_PERMANENT_BONUS = doublePreferencesKey("permanent_bonus")
        val KEY_UPGRADES = stringPreferencesKey("upgrades_csv")
    }

    override val gameState: Flow<GameState> = dataStore.data.map { prefs ->
        val upgradeLevels = parseUpgradeLevels(prefs[KEY_UPGRADES])
        GameState(
            currentRunId = prefs[KEY_RUN_ID] ?: "",
            activeWeaponFamily = prefs[KEY_WEAPON_FAMILY]?.let {
                runCatching { WeaponFamily.valueOf(it) }.getOrNull()
            } ?: WeaponFamily.RUNIC_GREATSWORD,
            sparks = prefs[KEY_SPARKS] ?: 0L,
            sparksPerTap = prefs[KEY_SPARKS_PER_TAP] ?: 1L,
            forgeLevel = prefs[KEY_FORGE_LEVEL] ?: 1,
            currentPhase = prefs[KEY_CURRENT_PHASE] ?: 1,
            sparksForNextPhase = prefs[KEY_SPARKS_FOR_NEXT] ?: 100L,
            totalSparksThisRun = prefs[KEY_TOTAL_SPARKS] ?: 0L,
            runNumber = prefs[KEY_RUN_NUMBER] ?: 1,
            permanentBonus = prefs[KEY_PERMANENT_BONUS] ?: 1.0,
            upgrades = defaultUpgrades().mapIndexed { i, u ->
                u.copy(level = upgradeLevels.getOrElse(i) { 0 })
            }
        )
    }

    override suspend fun saveGameState(state: GameState) {
        dataStore.edit { prefs ->
            prefs[KEY_RUN_ID] = state.currentRunId
            prefs[KEY_WEAPON_FAMILY] = state.activeWeaponFamily.name
            prefs[KEY_SPARKS] = state.sparks
            prefs[KEY_SPARKS_PER_TAP] = state.sparksPerTap
            prefs[KEY_FORGE_LEVEL] = state.forgeLevel
            prefs[KEY_CURRENT_PHASE] = state.currentPhase
            prefs[KEY_SPARKS_FOR_NEXT] = state.sparksForNextPhase
            prefs[KEY_TOTAL_SPARKS] = state.totalSparksThisRun
            prefs[KEY_RUN_NUMBER] = state.runNumber
            prefs[KEY_PERMANENT_BONUS] = state.permanentBonus
            prefs[KEY_UPGRADES] = state.upgrades.joinToString(",") { it.level.toString() }
        }
    }

    override suspend fun clearGameState() {
        dataStore.edit { it.clear() }
    }

    private fun parseUpgradeLevels(csv: String?): List<Int> {
        if (csv.isNullOrBlank()) return emptyList()
        return csv.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
