package com.forgelegends.domain.registry

import android.content.Context
import com.forgelegends.domain.model.Concept
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConceptRegistry @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val concepts: List<Concept> by lazy { discoverConcepts() }

    fun getById(id: String): Concept? = concepts.find { it.id == id }

    fun getByIdOrFirst(id: String): Concept =
        getById(id) ?: concepts.firstOrNull() ?: Concept(
            id = "unknown",
            name = "Unknown",
            emoji = "\u2753",
            description = ""
        )

    fun phaseImagePath(conceptId: String, phase: Int): String =
        "concepts/$conceptId/phase_$phase.png"

    fun angleImagePath(conceptId: String, angle: Int): String =
        "concepts/$conceptId/angle_$angle.png"

    fun hasPhaseImages(conceptId: String): Boolean {
        return try {
            context.assets.open(phaseImagePath(conceptId, 1)).close()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun angleCount(conceptId: String): Int {
        var count = 0
        while (count < 24) {
            try {
                context.assets.open(angleImagePath(conceptId, count)).close()
                count++
            } catch (_: Exception) {
                break
            }
        }
        return count
    }

    private fun discoverConcepts(): List<Concept> {
        val dirs = try {
            context.assets.list("concepts") ?: emptyArray()
        } catch (_: Exception) {
            emptyArray()
        }

        return dirs.mapNotNull { dir ->
            try {
                val json = context.assets.open("concepts/$dir/manifest.json")
                    .bufferedReader().use { it.readText() }
                val obj = JSONObject(json)
                val phaseCount = countFiles(dir, "phase_", 1)
                val angles = countFiles(dir, "angle_", 0)
                Concept(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    emoji = obj.getString("emoji"),
                    description = obj.optString("description", ""),
                    phaseCount = phaseCount,
                    angleCount = angles
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun countFiles(dir: String, prefix: String, startIndex: Int): Int {
        var count = 0
        var index = startIndex
        while (index < 30) {
            try {
                context.assets.open("concepts/$dir/$prefix$index.png").close()
                count++
                index++
            } catch (_: Exception) {
                break
            }
        }
        return count
    }
}
