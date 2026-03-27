package com.forgelegends.ui.components

import com.forgelegends.domain.model.WeaponFamily

object WeaponVisualRegistry {

    fun phaseLabel(family: WeaponFamily, phase: Int): String {
        val base = when (family) {
            WeaponFamily.RUNIC_GREATSWORD -> "Greatsword"
            WeaponFamily.WAR_HAMMER -> "War Hammer"
            WeaponFamily.ELVEN_BOW -> "Elven Bow"
        }
        return "$base - Phase $phase"
    }

    fun phaseEmoji(family: WeaponFamily, phase: Int): String {
        return when (family) {
            WeaponFamily.RUNIC_GREATSWORD -> when (phase) {
                1 -> "\uD83E\uDEA8" // rock
                2 -> "\uD83D\uDD29" // nut and bolt
                3 -> "\uD83D\uDDE1\uFE0F" // dagger
                4 -> "\u2694\uFE0F" // crossed swords
                5 -> "\uD83D\uDCAB" // dizzy (sparkle)
                6 -> "\uD83C\uDF1F" // glowing star
                else -> "\uD83C\uDFC6" // trophy
            }
            WeaponFamily.WAR_HAMMER -> when (phase) {
                1 -> "\uD83E\uDEA8"
                2 -> "\uD83D\uDD28"
                3 -> "\uD83D\uDD28"
                4 -> "\u2692\uFE0F"
                5 -> "\uD83D\uDCA5"
                6 -> "\uD83C\uDF1F"
                else -> "\uD83C\uDFC6"
            }
            WeaponFamily.ELVEN_BOW -> when (phase) {
                1 -> "\uD83C\uDF3F"
                2 -> "\uD83C\uDF3E"
                3 -> "\uD83C\uDFF9"
                4 -> "\uD83C\uDFF9"
                5 -> "\u2728"
                6 -> "\uD83C\uDF1F"
                else -> "\uD83C\uDFC6"
            }
        }
    }

    fun victoryLabel(family: WeaponFamily): String {
        return when (family) {
            WeaponFamily.RUNIC_GREATSWORD -> "Runic Greatsword of Legends"
            WeaponFamily.WAR_HAMMER -> "Thunderstrike War Hammer"
            WeaponFamily.ELVEN_BOW -> "Moonlight Elven Bow"
        }
    }
}
