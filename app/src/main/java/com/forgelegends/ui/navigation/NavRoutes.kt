package com.forgelegends.ui.navigation

object NavRoutes {
    const val FORGE = "forge"
    const val WORKBENCH = "workbench"
    const val WEAPON_PROGRESS = "weapon_progress"
    const val COMPLETION = "completion"
    const val SHOWCASE = "showcase"
    const val MODEL_DETAIL = "model_detail/{entryId}"
    const val WEAPON_SELECT = "weapon_select"

    fun modelDetail(entryId: String) = "model_detail/$entryId"
}
