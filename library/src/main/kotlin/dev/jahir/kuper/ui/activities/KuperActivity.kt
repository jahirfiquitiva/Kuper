package dev.jahir.kuper.ui.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.fondesa.kpermissions.PermissionStatus
import dev.jahir.frames.extensions.lazyViewModel
import dev.jahir.frames.ui.activities.FramesActivity
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.kuper.R
import dev.jahir.kuper.data.viewmodels.ComponentsViewModel
import dev.jahir.kuper.data.viewmodels.RequiredAppsViewModel
import dev.jahir.kuper.ui.fragments.ComponentsFragment
import dev.jahir.kuper.ui.fragments.KuperWallpapersFragment
import dev.jahir.kuper.ui.fragments.SetupFragment

abstract class KuperActivity : FramesActivity() {

    override val collectionsFragment: CollectionsFragment? = null
    override val favoritesFragment: WallpapersFragment? = null

    private val componentsFragment: ComponentsFragment by lazy { ComponentsFragment.create() }
    private val componentsViewModel: ComponentsViewModel by lazyViewModel()

    private val setupFragment: SetupFragment by lazy { SetupFragment.create() }
    private val requiredAppsViewModel: RequiredAppsViewModel by lazyViewModel()

    override val wallpapersFragment: WallpapersFragment? by lazy {
        KuperWallpapersFragment.create(ArrayList(wallpapersViewModel.wallpapers))
    }

    override val initialFragmentTag: String = SetupFragment.TAG
    override val initialItemId: Int = R.id.setup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStoragePermission()
        requiredAppsViewModel.observe(this) {
            if (it.isNotEmpty()) setupFragment.updateItems(it)
            else hideSetup()
        }
        componentsViewModel.observe(this) { componentsFragment.updateItems(it) }
        loadRequiredApps()
        loadComponents()
    }

    private fun hideSetup() {
        bottomNavigation?.removeItem(R.id.setup)
        if (currentItemId == R.id.setup) bottomNavigation?.selectedItemId = R.id.widgets
    }

    internal fun loadComponents() {
        componentsViewModel.loadComponents(this)
    }

    internal fun loadRequiredApps() {
        requiredAppsViewModel.loadApps(this)
    }

    override fun getNextFragment(itemId: Int): Pair<Pair<String?, Fragment?>?, Boolean>? =
        when (itemId) {
            R.id.setup -> Pair(Pair(SetupFragment.TAG, setupFragment), true)
            R.id.widgets -> Pair(Pair(ComponentsFragment.TAG, componentsFragment), true)
            else -> super.getNextFragment(itemId)
        }

    override fun internalOnPermissionsGranted(result: List<PermissionStatus>) {
        super.internalOnPermissionsGranted(result)
        (currentFragment as? ComponentsFragment)?.updateDeviceWallpaper()
        loadRequiredApps()
    }

    override fun canShowSearch(itemId: Int): Boolean = itemId != R.id.setup
    override fun canShowFavoritesButton(): Boolean = false
    override fun canModifyFavorites(): Boolean = false
}