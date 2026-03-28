package com.forgelegends.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.forgelegends.domain.model.Concept
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DataStoreCustomConceptRepository @Inject constructor(
    @Named("custom_concepts") private val dataStore: DataStore<Preferences>
) : CustomConceptRepository {

    private companion object {
        val KEY_CONCEPTS = stringPreferencesKey("custom_concepts_csv")
        const val FIELD_SEP = "|"
        const val ENTRY_SEP = ";"
    }

    override val concepts: Flow<List<Concept>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_CONCEPTS] ?: return@map emptyList()
        raw.split(ENTRY_SEP).mapNotNull { parseConcept(it) }
    }

    override suspend fun addConcept(concept: Concept) {
        dataStore.edit { prefs ->
            val existing = prefs[KEY_CONCEPTS] ?: ""
            // Avoid duplicates
            val current = existing.split(ENTRY_SEP).mapNotNull { parseConcept(it) }
            if (current.any { it.id == concept.id }) return@edit
            val serialized = serializeConcept(concept)
            prefs[KEY_CONCEPTS] = if (existing.isEmpty()) serialized else "$existing$ENTRY_SEP$serialized"
        }
    }

    private fun serializeConcept(c: Concept): String =
        listOf(c.id, c.name, c.emoji).joinToString(FIELD_SEP)

    private fun parseConcept(raw: String): Concept? {
        val parts = raw.split(FIELD_SEP)
        if (parts.size < 3) return null
        return runCatching {
            Concept(id = parts[0], name = parts[1], emoji = parts[2], description = "")
        }.getOrNull()
    }
}
