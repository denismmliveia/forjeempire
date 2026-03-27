package com.forgelegends.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.forgelegends.domain.model.WeaponFamily
import com.forgelegends.domain.model.WeaponShowcaseEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DataStoreWeaponShowcaseRepository @Inject constructor(
    @Named("weapon_showcase") private val dataStore: DataStore<Preferences>
) : WeaponShowcaseRepository {

    private companion object {
        val KEY_IDS = stringSetPreferencesKey("showcase_ids")
        val KEY_ENTRIES = stringPreferencesKey("showcase_entries")
        const val FIELD_SEP = "|"
        const val ENTRY_SEP = ";"
    }

    override val entries: Flow<List<WeaponShowcaseEntry>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_ENTRIES] ?: return@map emptyList()
        raw.split(ENTRY_SEP).mapNotNull { parseEntry(it) }
    }

    override suspend fun addIfAbsent(entry: WeaponShowcaseEntry) {
        dataStore.edit { prefs ->
            val ids = prefs[KEY_IDS]?.toMutableSet() ?: mutableSetOf()
            if (entry.id in ids) return@edit
            ids.add(entry.id)
            prefs[KEY_IDS] = ids
            val existing = prefs[KEY_ENTRIES] ?: ""
            val serialized = serializeEntry(entry)
            prefs[KEY_ENTRIES] = if (existing.isEmpty()) serialized else "$existing$ENTRY_SEP$serialized"
        }
    }

    private fun serializeEntry(e: WeaponShowcaseEntry): String {
        return listOf(
            e.id,
            e.runNumber.toString(),
            e.weaponFamily.name,
            e.completedAtEpochMillis.toString(),
            e.totalSparks.toString()
        ).joinToString(FIELD_SEP)
    }

    private fun parseEntry(raw: String): WeaponShowcaseEntry? {
        val parts = raw.split(FIELD_SEP)
        if (parts.size < 5) return null
        return runCatching {
            WeaponShowcaseEntry(
                id = parts[0],
                runNumber = parts[1].toInt(),
                weaponFamily = WeaponFamily.valueOf(parts[2]),
                completedAtEpochMillis = parts[3].toLong(),
                totalSparks = parts[4].toLong()
            )
        }.getOrNull()
    }
}
