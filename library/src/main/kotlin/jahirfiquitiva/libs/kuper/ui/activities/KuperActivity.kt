/*
 * Copyright (c) 2018. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jahirfiquitiva.libs.kuper.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.IntRange
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.restart
import ca.allanwang.kau.utils.visibleIf
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.kauextensions.extensions.PermissionRequestListener
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.changeOptionVisibility
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.inactiveIconsColor
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.requestSinglePermission
import jahirfiquitiva.libs.kauextensions.extensions.tint
import jahirfiquitiva.libs.kauextensions.ui.widgets.SearchView
import jahirfiquitiva.libs.kauextensions.ui.widgets.bindSearchView
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.ui.fragments.KuperFragment
import jahirfiquitiva.libs.kuper.ui.fragments.SetupFragment
import jahirfiquitiva.libs.kuper.ui.fragments.WallpapersFragment

abstract class KuperActivity : BaseFramesActivity() {
    
    private val toolbar: CustomToolbar by bind(R.id.toolbar)
    private val bottomNavigation: AHBottomNavigation by bind(R.id.bottom_navigation)
    
    private val fragments = ArrayList<Fragment>()
    
    private var searchView: SearchView? = null
    
    private var currentItemId = -1
    private var activeFragment: Fragment? = null
    
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kuper)
        toolbar.bindToActivity(this, false)
        setupContent()
    }
    
    fun hideSetup() {
        currentItemId = -1
        setupContent(false)
    }
    
    private fun setupContent(withSetup: Boolean = true) {
        postDelayed(
                100, {
            setupBottomNavigation(withSetup)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                showWallpaperPermissionExplanation()
            }
        })
    }
    
    override fun fragmentsContainer(): Int = R.id.fragments_container
    
    private fun setupBottomNavigation(withSetup: Boolean) {
        fragments.clear()
        if (withSetup)
            fragments += SetupFragment()
        fragments += KuperFragment()
        // fragments += WallpapersFragment()
        
        bottomNavigation.accentColor = accentColor
        with(bottomNavigation) {
            removeAllItems()
            
            defaultBackgroundColor = cardBackgroundColor
            inactiveColor = inactiveIconsColor
            isBehaviorTranslationEnabled = false
            isForceTint = true
            titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
            
            if (withSetup)
                addItem(AHBottomNavigationItem(getString(R.string.setup), R.drawable.ic_setup))
            
            addItem(AHBottomNavigationItem(getString(R.string.widgets), R.drawable.ic_widgets))
            
            if (getBoolean(R.bool.isKuper) && getString(R.string.json_url).hasContent())
                addItem(
                        AHBottomNavigationItem(
                                getString(R.string.wallpapers),
                                R.drawable.ic_all_wallpapers))
            
            setOnTabSelectedListener { position, _ ->
                navigateToItem(position)
            }
            setCurrentItem(if (currentItem < 0) 0 else currentItem, true)
            visibleIf(itemsCount >= 2)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.frames_menu, menu)
        
        menu?.let {
            it.changeOptionVisibility(R.id.donate, donationsEnabled && getBoolean(R.bool.isKuper))
            
            it.changeOptionVisibility(R.id.search, currentItemId != 0)
            
            it.changeOptionVisibility(R.id.refresh, currentItemId == 2)
            
            it.changeOptionVisibility(R.id.about, getBoolean(R.bool.isKuper))
            it.changeOptionVisibility(R.id.settings, getBoolean(R.bool.isKuper))
            
            searchView = bindSearchView(it, R.id.search)
            searchView?.listener = object : SearchView.SearchListener {
                override fun onQueryChanged(query: String) {
                    doSearch(query)
                }
                
                override fun onQuerySubmit(query: String) {
                    doSearch(query)
                }
                
                override fun onSearchOpened(searchView: SearchView) {}
                
                override fun onSearchClosed(searchView: SearchView) {
                    doSearch()
                }
            }
            val hint = bottomNavigation.getItem(currentItemId)?.getTitle(this).orEmpty()
            searchView?.hintText = getString(R.string.search_x, hint.toLowerCase())
        }
        
        toolbar.tint(
                getPrimaryTextColorFor(primaryColor, 0.6F),
                getSecondaryTextColorFor(primaryColor, 0.6F),
                getActiveIconsColorFor(primaryColor, 0.6F))
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            val id = it.itemId
            when (id) {
                R.id.refresh -> refreshContent()
                R.id.about -> startActivity(Intent(this, CreditsActivity::class.java))
                R.id.settings -> startActivityForResult(
                        Intent(this, SettingsActivity::class.java),
                        22)
                R.id.donate -> doDonation()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun navigateToItem(
            @IntRange(from = 0, to = 2) position: Int
                              ): Boolean {
        invalidateOptionsMenu()
        
        try {
            val hasSetup = (bottomNavigation.itemsCount > 2)
            if (!isFragmentValid(position) || currentItemId != position) {
                activeFragment = when (position) {
                    0 -> {
                        if (hasSetup) SetupFragment()
                        else fragments[position]
                    }
                    1 -> {
                        if (hasSetup) fragments[position]
                        else WallpapersFragment()
                    }
                    2 -> WallpapersFragment()
                    else -> null
                }
                activeFragment?.let {
                    changeFragment(it)
                    currentItemId = position
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }
    
    private fun isFragmentValid(position: Int): Boolean {
        return when (position) {
            0 -> activeFragment is SetupFragment
            1 -> activeFragment is KuperFragment
            2 -> activeFragment is WallpapersFragment
            else -> true
        }
    }
    
    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt("current", currentItemId)
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        currentItemId = savedInstanceState?.getInt("current", -1) ?: -1
        postDelayed(100, { navigateToItem(currentItemId) })
    }
    
    private val LOCK = Any()
    private fun doSearch(filter: String = "") {
        synchronized(
                LOCK, {
            postDelayed(
                    200, {
                if (activeFragment is KuperFragment) {
                    (activeFragment as KuperFragment).applyFilter(filter)
                } else if (activeFragment is BaseFramesFragment<*, *>) {
                    (activeFragment as BaseFramesFragment<*, *>)
                            .enableRefresh(!filter.hasContent())
                    (activeFragment as BaseFramesFragment<*, *>).applyFilter(filter)
                }
            })
        })
    }
    
    private fun refreshContent() {
        if (activeFragment is WallpapersFragment) {
            (activeFragment as WallpapersFragment).reloadData(1)
        }
    }
    
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>,
            grantResults: IntArray
                                           ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 43) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                installAssets()
            } else {
                showSnackbar(getString(R.string.permission_denied), Snackbar.LENGTH_LONG)
            }
        } else if (requestCode == 55) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                restart()
            } else {
                showSnackbar(getString(R.string.permission_denied), Snackbar.LENGTH_LONG)
            }
        }
    }
    
    fun requestPermissionWallpaper() {
        requestSinglePermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE, 55,
                object : PermissionRequestListener() {
                    override fun onShowInformation(permission: String) =
                            showWallpaperPermissionExplanation()
                    
                    override fun onPermissionCompletelyDenied() =
                            showSnackbar(
                                    getString(R.string.permission_denied_completely),
                                    Snackbar.LENGTH_LONG)
                    
                    override fun onPermissionGranted() = restart()
                })
    }
    
    private fun showWallpaperPermissionExplanation() {
        showSnackbar(
                getString(R.string.permission_request_wallpaper, getAppName()),
                Snackbar.LENGTH_LONG) {
            setAction(
                    R.string.allow, {
                dismiss()
                requestPermissionWallpaper()
            })
        }
    }
    
    fun requestPermissionInstallAssets() {
        requestSinglePermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE, 43,
                object : PermissionRequestListener() {
                    override fun onShowInformation(permission: String) =
                            showAssetsPermissionExplanation()
                    
                    override fun onPermissionCompletelyDenied() =
                            showSnackbar(
                                    getString(R.string.permission_denied_completely),
                                    Snackbar.LENGTH_LONG)
                    
                    override fun onPermissionGranted() = installAssets()
                })
    }
    
    private fun showAssetsPermissionExplanation() {
        showSnackbar(
                getString(R.string.permission_request_assets, getAppName()),
                Snackbar.LENGTH_LONG) {
            setAction(
                    R.string.allow, {
                dismiss()
                requestPermissionInstallAssets()
            })
        }
    }
    
    fun installAssets() {
        // TODO Finish
        /*
        val folders = arrayOf("fonts", "iconsets", "bitmaps")
        val actualFolders = ArrayList<String>()
        folders.forEach { if (inAssetsAndWithContent(it)) actualFolders.add(it) }
        
        var count = 0
        
        actualFolders.forEachIndexed { index, s ->
            destroyDialog()
            val dialogContent = getString(R.string.copying_assets, getCorrectFolderName(s))
            dialog = buildMaterialDialog {
                content(dialogContent)
                progress(true, 0)
                cancelable(false)
            }
            dialog?.setOnShowListener {
                CopyAssetsTask(
                        WeakReference(this), s, {
                    if (it) count += 1
                    destroyDialog()
                    if (index == actualFolders.size - 1) {
                        showSnackbar(
                                getString(
                                        if (count == actualFolders.size) R.string.copied_assets_successfully
                                        else R.string.copied_assets_error), Snackbar.LENGTH_LONG)
                        if (count == actualFolders.size) {
                            val item: KuperApp? = apps.firstOrNull { !(it.packageName.hasContent()) }
                            item?.let {
                                apps.remove(it)
                                if (activeFragment is SetupFragment) {
                                    (activeFragment as SetupFragment).updateList()
                                }
                            }
                        }
                    }
                }).execute()
            }
            dialog?.show()
        }
        */
    }
}