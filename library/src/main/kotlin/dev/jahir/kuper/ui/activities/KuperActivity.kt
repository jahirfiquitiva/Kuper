package dev.jahir.kuper.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.extensions.context.getAppName
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.utils.lazyViewModel
import dev.jahir.frames.extensions.utils.postDelayed
import dev.jahir.frames.ui.activities.FramesActivity
import dev.jahir.frames.ui.activities.base.PermissionsResult
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.kuper.R
import dev.jahir.kuper.data.viewmodels.RequiredAppsViewModel
import dev.jahir.kuper.ui.fragments.ComponentsFragment
import dev.jahir.kuper.ui.fragments.KuperWallpapersFragment
import dev.jahir.kuper.ui.fragments.SetupFragment

abstract class KuperActivity : FramesActivity() {

    override val wallpapersViewModel: WallpapersDataViewModel by lazyViewModel()

    override val collectionsFragment: CollectionsFragment? = null
    override val favoritesFragment: WallpapersFragment? = null

    override val wallpapersFragment: WallpapersFragment? by lazy {
        KuperWallpapersFragment.create(ArrayList(wallpapersViewModel.wallpapers))
    }

    override val initialFragmentTag: String = SetupFragment.TAG
    override val initialItemId: Int = R.id.setup

    private val componentsFragment: ComponentsFragment by lazy { ComponentsFragment() }

    private val requiredAppsViewModel: RequiredAppsViewModel by lazyViewModel()
    private val setupFragment: SetupFragment by lazy { SetupFragment.create(requiredAppsViewModel.apps) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requiredAppsViewModel.observe(this) {
            if (it.isNotEmpty()) {
                setupFragment.apply {
                    updateItems(it)
                    cleanRecyclerViewState()
                }
            } else hideSetup()
        }
        loadRequiredApps()
        requestStoragePermission()
    }

    override fun shouldShowToolbarLogo(itemId: Int): Boolean {
        val setupShown = bottomNavigation?.menu?.findItem(R.id.setup)?.isVisible ?: false
        return itemId == if (setupShown) R.id.setup else R.id.widgets
    }

    override fun onSafeBackPressed() {
        val setupShown = bottomNavigation?.menu?.findItem(R.id.setup)?.isVisible ?: false
        val actualInitialItemId = if (setupShown) R.id.setup else R.id.widgets
        if (currentItemId != actualInitialItemId)
            bottomNavigation?.selectedItemId = actualInitialItemId
        else supportFinishAfterTransition()
    }

    override fun onDestroy() {
        super.onDestroy()
        requiredAppsViewModel.destroy(this)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.settings -> {
            startActivity(Intent(this, KuperSettingsActivity::class.java))
            true
        }
        R.id.about -> {
            startActivity(Intent(this, KuperAboutActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun hideSetup() {
        bottomNavigation?.removeItem(R.id.setup)
        if (currentItemId == R.id.setup) {
            bottomNavigation?.selectedItemId = R.id.widgets
            postDelayed(10) { componentsFragment.loadData() }
        }
        updateToolbarTitle(currentItemId)
    }

    internal fun loadRequiredApps() {
        requiredAppsViewModel.loadApps()
    }

    override fun getNextFragment(itemId: Int): Pair<Pair<String?, Fragment?>?, Boolean>? =
        when (itemId) {
            R.id.setup -> Pair(Pair(SetupFragment.TAG, setupFragment), true)
            R.id.widgets -> Pair(Pair(ComponentsFragment.TAG, componentsFragment), true)
            else -> super.getNextFragment(itemId)
        }

    override fun getToolbarTitleForItem(itemId: Int): String? {
        val setupShown = bottomNavigation?.menu?.findItem(R.id.setup)?.isVisible == true
        return when (itemId) {
            R.id.widgets -> if (setupShown) string(R.string.widgets) else getAppName()
            R.id.wallpapers -> string(R.string.wallpapers)
            else -> super.getToolbarTitleForItem(itemId)
        }
    }

    override fun internalOnPermissionsGranted(permission: String) {
        super.internalOnPermissionsGranted(permission)
        if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            (currentFragment as? ComponentsFragment)?.updateDeviceWallpaper()
            loadRequiredApps()
        }
    }

    override fun getPermissionRationaleMessage(permissions: PermissionsResult): String {
        return if (permissions.storage) {
            when (currentItemId) {
                R.id.widgets -> string(R.string.permission_request_wallpaper, getAppName())
                else -> super.getPermissionRationaleMessage(permissions)
            }
        } else super.getPermissionRationaleMessage(permissions)
    }

    override fun canShowSearch(itemId: Int): Boolean = itemId != R.id.setup
    override fun canShowFavoritesButton(): Boolean = false
    override fun canModifyFavorites(): Boolean = false

    override fun shouldLoadCollections(): Boolean = false
    override fun shouldLoadFavorites(): Boolean = false
}
