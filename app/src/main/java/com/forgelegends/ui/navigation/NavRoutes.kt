package com.forgelegends.ui.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val FORGE = "forge"
    const val HOLO_LAB = "holo_lab"
    const val LAYER_PROGRESS = "layer_progress"
    const val COMPLETION = "completion"
    const val HOLO_GALLERY = "holo_gallery"
    const val HOLO_VIEWER = "holo_viewer/{entryId}"
    const val BLUEPRINT_SELECT = "blueprint_select"

    fun holoViewer(entryId: String) = "holo_viewer/$entryId"
}
