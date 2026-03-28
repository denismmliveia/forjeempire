package com.forgelegends.domain.model

data class Concept(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val phaseCount: Int = 6,
    val angleCount: Int = 0
)
