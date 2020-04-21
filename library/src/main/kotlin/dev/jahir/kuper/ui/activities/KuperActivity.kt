package dev.jahir.kuper.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.fondesa.kpermissions.PermissionStatus
import com.github.javiersantos.piracychecker.activities.getAppName
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.utils.lazyViewModel
import dev.jahir.frames.extensions.utils.postDelayed
import dev.jahir.frames.extensions.views.snackbar
import dev.jahir.frames.ui.activities.FramesActivity
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.kuper.R
import dev.jahir.kuper.data.tasks.KuperAssets
import dev.jahir.kuper.data.viewmodels.RequiredAppsViewModel
import dev.jahir.kuper.extensions.hasStoragePermission
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

    private var shouldInstallAssets = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requiredAppsViewModel.observe(this) {
            if (it.isNotEmpty()) setupFragment.updateItems(it)
            else hideSetup()
        }

        loadRequiredApps()
        requestStoragePermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        requiredAppsViewModel.destroy(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            startActivity(Intent(this, KuperSettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hideSetup() {
        bottomNavigation?.removeItem(R.id.setup)
        if (currentItemId == R.id.setup) bottomNavigation?.selectedItemId = R.id.widgets
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

    override fun internalOnPermissionsGranted(result: List<PermissionStatus>) {
        super.internalOnPermissionsGranted(result)
        (currentFragment as? ComponentsFragment)?.updateDeviceWallpaper()
        loadRequiredApps()
        if (shouldInstallAssets) internalInstallAssets()
    }

    override fun getPermissionRationaleMessage(): String =
        when (currentItemId) {
            R.id.setup -> string(R.string.permission_request_assets, getAppName())
            R.id.widgets -> string(R.string.permission_request_wallpaper, getAppName())
            else -> super.getPermissionRationaleMessage()
        }

    override fun canShowSearch(itemId: Int): Boolean = itemId != R.id.setup
    override fun canShowFavoritesButton(): Boolean = false
    override fun canModifyFavorites(): Boolean = false

    internal fun requestPermissionToInstallAssets() {
        if (hasStoragePermission) internalInstallAssets()
        else {
            shouldInstallAssets = true
            requestStoragePermission()
        }
    }

    private fun internalInstallAssets() {
        shouldInstallAssets = false
        KuperAssets.copyZooperAssets(this) { success ->
            snackbar(
                string(
                    if (success) R.string.copied_assets_successfully
                    else R.string.copied_assets_error
                ),
                Snackbar.LENGTH_LONG
            ) {
                addCallback(object : Snackbar.Callback() {
                    override fun onShown(sb: Snackbar?) {
                        super.onShown(sb)
                        postDelayed(100) { loadRequiredApps() }
                    }
                })
            }
        }
    }

    override fun shouldLoadCollections(): Boolean = false
    override fun shouldLoadFavorites(): Boolean = false
}