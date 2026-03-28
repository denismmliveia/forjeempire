package com.forgelegends.data.repository

import com.forgelegends.domain.model.Concept
import kotlinx.coroutines.flow.Flow

interface CustomConceptRepository {
    val concepts: Flow<List<Concept>>
    suspend fun addConcept(concept: Concept)
}
