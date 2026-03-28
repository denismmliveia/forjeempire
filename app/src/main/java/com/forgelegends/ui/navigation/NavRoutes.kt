package com.forgelegends.ui.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val FORGE = "forge"
    const val WORKBENCH = "workbench"
    const val WEAPON_PROGRESS = "weapon_progress"
    const val COMPLETION = "completion"
    const val SHOWCASE = "showcase"
    const val MODEL_DETAIL = "model_detail/{entryId}"
    const val CONCEPT_SELECT = "concept_select"

    fun modelDetail(entryId: String) = "model_detail/$entryId"
}
