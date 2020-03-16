package dev.jahir.kuper.ui.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.jahir.frames.extensions.lazyViewModel
import dev.jahir.frames.ui.activities.FramesActivity
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.kuper.R
import dev.jahir.kuper.data.viewmodels.ComponentsViewModel
import dev.jahir.kuper.ui.fragments.ComponentsFragment
import dev.jahir.kuper.ui.fragments.KuperWallpapersFragment

abstract class KuperActivity : FramesActivity() {

    override val collectionsFragment: CollectionsFragment? = null
    override val favoritesFragment: WallpapersFragment? = null

    private val componentsFragment: ComponentsFragment by lazy { ComponentsFragment.create() }
    private val componentsViewModel: ComponentsViewModel by lazyViewModel()

    override val wallpapersFragment: WallpapersFragment? by lazy {
        KuperWallpapersFragment.create(ArrayList(wallpapersViewModel.wallpapers))
    }

    override val initialFragmentTag: String = ComponentsFragment.TAG
    override val initialItemId: Int = R.id.widgets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStoragePermission()
        componentsViewModel.observe(this) { componentsFragment.updateItems(it) }
        loadComponents()
    }

    internal fun loadComponents() {
        componentsViewModel.loadComponents(this)
    }

    override fun getNextFragment(itemId: Int): Pair<Pair<String?, Fragment?>?, Boolean>? =
        when (itemId) {
            R.id.widgets -> Pair(Pair(ComponentsFragment.TAG, componentsFragment), true)
            else -> super.getNextFragment(itemId)
        }

    override fun canShowFavoritesButton(): Boolean = false
    override fun canModifyFavorites(): Boolean = false
}