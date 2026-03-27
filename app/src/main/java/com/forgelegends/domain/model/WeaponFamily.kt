package com.forgelegends.domain.model

enum class WeaponFamily {
    RUNIC_GREATSWORD,
    WAR_HAMMER,
    ELVEN_BOW;

    fun next(): WeaponFamily {
        val values = entries
        return values[(ordinal + 1) % values.size]
    }
}
