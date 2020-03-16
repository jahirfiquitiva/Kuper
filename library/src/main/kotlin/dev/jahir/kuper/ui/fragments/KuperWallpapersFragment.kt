package dev.jahir.kuper.ui.fragments

import android.content.Intent
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.kuper.ui.activities.KuperViewerActivity

class KuperWallpapersFragment : WallpapersFragment() {

    override val canShowFavoritesButton: Boolean = false
    override fun getTargetActivityIntent(): Intent =
        Intent(activity, KuperViewerActivity::class.java)

    companion object {
        @JvmStatic
        fun create(list: ArrayList<Wallpaper> = ArrayList()) = KuperWallpapersFragment().apply {
            this.isForFavs = false
            notifyCanModifyFavorites(false)
            updateItemsInAdapter(list)
        }
    }
}