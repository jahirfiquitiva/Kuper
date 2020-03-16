package dev.jahir.kuper.ui.activities

import dev.jahir.frames.ui.activities.ViewerActivity

class KuperViewerActivity : ViewerActivity() {
    override fun canShowFavoritesButton(): Boolean = false
    override fun canModifyFavorites(): Boolean = false
}