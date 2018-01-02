/*
 * Copyright (c) 2017. Jahir Fiquitiva
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
import android.os.Environment
import android.support.annotation.IntRange
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.utils.isAppInstalled
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.restart
import ca.allanwang.kau.utils.visibleIf
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
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
import jahirfiquitiva.libs.kuper.helpers.utils.CopyAssetsTask
import jahirfiquitiva.libs.kuper.helpers.utils.CopyAssetsTask.Companion.filesToIgnore
import jahirfiquitiva.libs.kuper.helpers.utils.CopyAssetsTask.Companion.getCorrectFolderName
import jahirfiquitiva.libs.kuper.helpers.utils.KLWP_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KOLORETTE_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KWGT_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.MEDIA_UTILS_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.ZOOPER_PACKAGE
import jahirfiquitiva.libs.kuper.ui.adapters.KuperApp
import jahirfiquitiva.libs.kuper.ui.fragments.KuperFragment
import jahirfiquitiva.libs.kuper.ui.fragments.SetupFragment
import jahirfiquitiva.libs.kuper.ui.fragments.WallpapersFragment
import java.io.File
import java.lang.ref.WeakReference

abstract class KuperActivity : BaseFramesActivity() {
    
    private val SETUP_KEY = "setup"
    private val WIDGETS_KEY = "widgets"
    private val WALLPAPERS_KEY = "wallpapers"
    
    private val toolbar: CustomToolbar by bind(R.id.toolbar)
    private val bottomNavigation: AHBottomNavigation by bind(R.id.bottom_navigation)
    
    val apps = ArrayList<KuperApp>()
    private val fragments = ArrayList<Fragment>()
    
    private var searchView: SearchView? = null
    
    private var currentItemId = -1
    private var currentFragment: Fragment? = null
    
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kuper)
        toolbar.bindToActivity(this, false)
        setupContent()
    }
    
    private fun setupContent() {
        setupApps()
        
        postDelayed(
                100, {
            setupBottomNavigation()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                showWallpaperPermissionExplanation()
            }
        })
    }
    
    @SuppressLint("MissingSuperCall")
    override fun onResume() {
        super.onResume()
        setupApps()
        if (apps.isEmpty()) setupBottomNavigation()
    }
    
    override fun fragmentsContainer(): Int = R.id.fragments_container
    
    private fun setupApps() {
        apps.clear()
        
        if (!isAppInstalled(ZOOPER_PACKAGE)) {
            apps.add(
                    KuperApp(
                            getString(R.string.zooper_widget),
                            getString(R.string.required_for_widgets),
                            "ic_zooper", ZOOPER_PACKAGE))
        }
        
        if (!isAppInstalled(MEDIA_UTILS_PACKAGE) && getBoolean(R.bool.media_utils_required)) {
            apps.add(
                    KuperApp(
                            getString(R.string.media_utils),
                            getString(R.string.required_for_widgets),
                            "ic_zooper", MEDIA_UTILS_PACKAGE))
        }
        
        if (!isAppInstalled(KOLORETTE_PACKAGE) && getBoolean(R.bool.kolorette_required)) {
            apps.add(
                    KuperApp(
                            getString(R.string.kolorette),
                            getString(R.string.required_for_widgets),
                            "ic_zooper", KOLORETTE_PACKAGE))
        }
        
        if (!isAppInstalled(KWGT_PACKAGE) && inAssetsAndWithContent("widgets")) {
            apps.add(
                    KuperApp(
                            getString(R.string.kwgt),
                            getString(R.string.required_for_widgets),
                            "ic_kustom", KWGT_PACKAGE))
        }
        
        if (!isAppInstalled(KLWP_PACKAGE) && inAssetsAndWithContent("wallpapers")) {
            apps.add(
                    KuperApp(
                            getString(R.string.klwp),
                            getString(R.string.required_for_wallpapers),
                            "ic_kustom", KLWP_PACKAGE))
        }
        
        if (!areAssetsInstalled()) {
            apps.add(
                    KuperApp(
                            getString(R.string.zooper_widget),
                            getString(R.string.required_assets),
                            "ic_zooper"))
        }
        
        if (currentFragment is SetupFragment) {
            (currentFragment as SetupFragment).updateList()
        }
    }
    
    private fun setupBottomNavigation() {
        fragments += SetupFragment()
        fragments += KuperFragment()
        fragments += WallpapersFragment()
        
        bottomNavigation.accentColor = accentColor
        with(bottomNavigation) {
            removeAllItems()
            
            defaultBackgroundColor = cardBackgroundColor
            inactiveColor = inactiveIconsColor
            isBehaviorTranslationEnabled = false
            isForceTint = true
            titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
            
            if (apps.isNotEmpty())
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
            val donationItem = it.findItem(R.id.donate)
            donationItem?.isVisible = donationsEnabled
            
            it.changeOptionVisibility(
                    R.id.search,
                    currentItemId != (if (apps.isNotEmpty()) 0 else -1))
            
            it.changeOptionVisibility(
                    R.id.refresh,
                    currentItemId == (if (apps.isNotEmpty()) 2 else 1))
            
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
            val correctPosition = if (bottomNavigation.itemsCount >= 2 && apps.isEmpty()) position + 1 else position
            if (!isFragmentValid(correctPosition) || currentItemId != position) {
                currentFragment = when (correctPosition) {
                    0, 1 -> fragments[correctPosition]
                    2 -> WallpapersFragment()
                    else -> null
                }
                currentFragment?.let {
                    changeFragment(it)
                    currentItemId = position
                }
            }
        } catch (ignored: Exception) {
        }
        return true
    }
    
    private fun isFragmentValid(position: Int): Boolean {
        return when (position) {
            0 -> currentFragment is SetupFragment
            1 -> currentFragment is KuperFragment
            2 -> currentFragment is WallpapersFragment
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
                if (currentFragment is KuperFragment) {
                    (currentFragment as KuperFragment).applyFilter(filter)
                } else if (currentFragment is BaseFramesFragment<*, *>) {
                    (currentFragment as BaseFramesFragment<*, *>)
                            .enableRefresh(!filter.hasContent())
                    (currentFragment as BaseFramesFragment<*, *>).applyFilter(filter)
                }
            })
        })
    }
    
    private fun refreshContent() {
        if (currentFragment is WallpapersFragment) {
            (currentFragment as WallpapersFragment).reloadData(1)
        }
    }
    
    private fun inAssetsAndWithContent(folder: String): Boolean {
        val folders = assets.list("")
        return if (folders != null) {
            if (folders.contains(folder)) {
                return getFilesInAssetsFolder(folder).isNotEmpty()
            } else false
        } else false
    }
    
    private fun getFilesInAssetsFolder(folder: String): ArrayList<String> {
        val list = ArrayList<String>()
        val files = assets.list(folder)
        if (files != null) {
            if (files.isNotEmpty()) {
                files.forEach {
                    if (!(filesToIgnore.contains(it))) list.add(it)
                }
            }
        }
        return list
    }
    
    private fun areAssetsInstalled(): Boolean {
        val folders = arrayOf("fonts", "iconsets", "bitmaps")
        val actualFolders = ArrayList<String>()
        folders.forEach { if (inAssetsAndWithContent(it)) actualFolders.add(it) }
        
        var count = 0
        
        for (folder in actualFolders) {
            var filesCount = 0
            val possibleFiles = getFilesInAssetsFolder(folder)
            possibleFiles.forEach {
                if (it.contains(".") && (!filesToIgnore.contains(it))) {
                    val file = File(
                            "${Environment.getExternalStorageDirectory()}/ZooperWidget/${getCorrectFolderName(
                                    folder)}/$it")
                    if (file.exists()) filesCount += 1
                }
            }
            if (filesCount == possibleFiles.size) count += 1
        }
        
        return count == actualFolders.size
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
                Snackbar.LENGTH_LONG, {
                    setAction(
                            R.string.allow, {
                        dismiss()
                        requestPermissionWallpaper()
                    })
                })
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
                Snackbar.LENGTH_LONG, {
                    setAction(
                            R.string.allow, {
                        dismiss()
                        requestPermissionInstallAssets()
                    })
                })
    }
    
    fun installAssets() {
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
                                if (currentFragment is SetupFragment) {
                                    (currentFragment as SetupFragment).updateList()
                                }
                            }
                        }
                    }
                }).execute()
            }
            dialog?.show()
        }
    }
}