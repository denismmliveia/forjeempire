package com.forgelegends.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DataStorePlayerProgressRepository @Inject constructor(
    @Named("player_progress") private val dataStore: DataStore<Preferences>
) : PlayerProgressRepository {

    private companion object {
        val KEY_SEED = longPreferencesKey("player_seed")
        val KEY_QUEUE_POS = intPreferencesKey("queue_position")
        val KEY_UNLOCKED_SECRETS = stringPreferencesKey("unlocked_secret_ids")
    }

    override val playerSeed: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_SEED] ?: 0L
    }

    override val queuePosition: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_QUEUE_POS] ?: 0
    }

    override val unlockedSecretIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_UNLOCKED_SECRETS] ?: ""
        if (raw.isEmpty()) emptySet() else raw.split(",").toSet()
    }

    override suspend fun initSeedIfAbsent() {
        dataStore.edit { prefs ->
            if (prefs[KEY_SEED] == null) {
                prefs[KEY_SEED] = Random.nextLong()
            }
        }
    }

    override suspend fun advanceQueue() {
        dataStore.edit { prefs ->
            val current = prefs[KEY_QUEUE_POS] ?: 0
            prefs[KEY_QUEUE_POS] = current + 1
        }
    }

    override suspend fun addUnlockedSecret(conceptId: String) {
        dataStore.edit { prefs ->
            val raw = prefs[KEY_UNLOCKED_SECRETS] ?: ""
            val existing = if (raw.isEmpty()) emptySet() else raw.split(",").toSet()
            if (conceptId !in existing) {
                prefs[KEY_UNLOCKED_SECRETS] = (existing + conceptId).joinToString(",")
            }
        }
    }
}
