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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.IntRange
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.utils.boolean
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.visibleIf
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.changeOptionVisibility
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.inactiveIconsColor
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.tint
import jahirfiquitiva.libs.kauextensions.ui.fragments.adapters.FragmentsPagerAdapter
import jahirfiquitiva.libs.kauextensions.ui.widgets.CustomSearchView
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.helpers.extensions.inAssetsAndWithContent
import jahirfiquitiva.libs.kuper.helpers.extensions.kuperKonfigs
import jahirfiquitiva.libs.kuper.helpers.utils.CopyAssetsTask
import jahirfiquitiva.libs.kuper.helpers.utils.KuperLog
import jahirfiquitiva.libs.kuper.ui.fragments.KuperFragment
import jahirfiquitiva.libs.kuper.ui.fragments.SetupFragment
import jahirfiquitiva.libs.kuper.ui.fragments.WallpapersFragment
import jahirfiquitiva.libs.kuper.ui.widgets.PseudoViewPager
import java.lang.ref.WeakReference

abstract class KuperActivity : BaseFramesActivity() {
    
    private val toolbar: CustomToolbar? by bind(R.id.toolbar)
    private val bottomNavigation: AHBottomNavigation? by bind(R.id.bottom_navigation)
    
    private var searchItem: MenuItem? = null
    private var searchView: CustomSearchView? = null
    
    private val pager: PseudoViewPager? by bind(R.id.pager)
    private var currentItemId = 0
    private var withSetup = true
    
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kuper)
        toolbar?.bindToActivity(this, !boolean(R.bool.isKuper))
        toolbar?.title = getActivityTitle()
        supportActionBar?.title = getActivityTitle()
        setupContent()
    }
    
    override fun onPause() {
        super.onPause()
        if (searchView?.isOpen == true) searchItem?.collapseActionView()
    }
    
    open fun getActivityTitle(): String = getAppName()
    
    fun hideSetup() {
        currentItemId = 0
        setupContent(false)
    }
    
    private fun setupContent(withSetup: Boolean = true) {
        this.withSetup = withSetup
        postDelayed(50) {
            setupBottomNavigation()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                requestStoragePermission(
                        getString(R.string.permission_request_wallpaper, getAppName()).orEmpty()) {
                    if (!kuperKonfigs.permissionRequested) {
                        kuperKonfigs.permissionRequested = true
                        onThemeChanged()
                    }
                }
            }
        }
    }
    
    override fun fragmentsContainer(): Int = 0
    
    private fun initPagerAdapter() {
        pager?.adapter = if (withSetup) {
            FragmentsPagerAdapter(
                    supportFragmentManager,
                    SetupFragment(),
                    KuperFragment(),
                    WallpapersFragment.create(getLicenseChecker() != null))
        } else {
            FragmentsPagerAdapter(
                    supportFragmentManager,
                    KuperFragment(),
                    WallpapersFragment.create(getLicenseChecker() != null))
        }
    }
    
    private fun setupBottomNavigation() {
        initPagerAdapter()
        
        pager?.offscreenPageLimit = pager?.adapter?.count ?: 2
        
        bottomNavigation?.let {
            it.accentColor = accentColor
            with(it) {
                removeAllItems()
                
                defaultBackgroundColor = cardBackgroundColor
                inactiveColor = inactiveIconsColor
                isBehaviorTranslationEnabled = false
                isForceTint = true
                titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
                
                if (withSetup)
                    addItem(AHBottomNavigationItem(string(R.string.setup), R.drawable.ic_setup))
                
                addItem(AHBottomNavigationItem(string(R.string.widgets), R.drawable.ic_widgets))
                
                if (boolean(R.bool.isKuper) && string(R.string.json_url).hasContent())
                    addItem(
                            AHBottomNavigationItem(
                                    string(R.string.wallpapers), R.drawable.ic_all_wallpapers))
                
                setOnTabSelectedListener { position, _ ->
                    navigateToItem(position)
                }
                setCurrentItem(if (currentItem < 0) 0 else currentItem, true)
                visibleIf(itemsCount >= 2)
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.frames_menu, menu)
        
        menu?.let {
            it.changeOptionVisibility(R.id.donate, donationsEnabled && boolean(R.bool.isKuper))
            it.changeOptionVisibility(R.id.search, currentItemId != 0)
            it.changeOptionVisibility(R.id.refresh, currentItemId == 2)
            
            it.changeOptionVisibility(R.id.about, boolean(R.bool.isKuper))
            it.changeOptionVisibility(R.id.settings, boolean(R.bool.isKuper))
            
            searchItem = it.findItem(R.id.search)
            searchView = searchItem?.actionView as? CustomSearchView
            searchView?.onCollapse = { doSearch() }
            searchView?.onQueryChanged = { doSearch(it) }
            searchView?.onQuerySubmit = { doSearch(it) }
            searchView?.bindToItem(searchItem)
            
            val hint = bottomNavigation?.getItem(currentItemId)?.getTitle(this).orEmpty()
            searchView?.queryHint =
                    if (hint.hasContent()) getString(R.string.search_x, hint.toLowerCase())
                    else string(R.string.search)
            
            searchView?.tint(getPrimaryTextColorFor(primaryColor, 0.6F))
            it.tint(getActiveIconsColorFor(primaryColor, 0.6F))
        }
        
        toolbar?.tint(
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
                R.id.settings ->
                    startActivityForResult(Intent(this, SettingsActivity::class.java), 22)
                R.id.donate -> doDonation()
                android.R.id.home -> finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun navigateToItem(@IntRange(from = 0, to = 2) position: Int): Boolean {
        return try {
            if (currentItemId != position) {
                pager?.currentItem = position
                currentItemId = position
                invalidateOptionsMenu()
                true
            } else {
                val activeFragment =
                        (pager?.adapter as? FragmentsPagerAdapter)?.get(pager?.currentItem ?: -1)
                (activeFragment as? SetupFragment)?.scrollToTop()
                (activeFragment as? KuperFragment)?.scrollToTop()
                (activeFragment as? WallpapersFragment)?.scrollToTop()
                false
            }
        } catch (e: Exception) {
            KuperLog.e { e.message }
            false
        }
    }
    
    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt("current", currentItemId)
        outState?.putBoolean("withSetup", withSetup)
        super.onSaveInstanceState(outState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        invalidateOptionsMenu()
        currentItemId = savedInstanceState?.getInt("current", 0) ?: 0
        withSetup = savedInstanceState?.getBoolean("withSetup", false) ?: false
        initPagerAdapter()
        navigateToItem(currentItemId)
    }
    
    private val lock = Any()
    private fun doSearch(filter: String = "") {
        synchronized(lock) {
            postDelayed(200) {
                val activeFragment =
                        (pager?.adapter as? FragmentsPagerAdapter)?.get(pager?.currentItem ?: -1)
                if (activeFragment is KuperFragment) {
                    activeFragment.applyFilter(filter)
                } else if (activeFragment is BaseFramesFragment<*, *>) {
                    activeFragment.enableRefresh(!filter.hasContent())
                    activeFragment.applyFilter(filter)
                }
            }
        }
    }
    
    private fun refreshContent() {
        val activeFragment =
                (pager?.adapter as? FragmentsPagerAdapter)?.get(pager?.currentItem ?: -1)
        (activeFragment as? WallpapersFragment)?.reloadData(1)
    }
    
    fun installAssets() {
        val folders = arrayOf("fonts", "iconsets", "bitmaps")
        val actualFolders = ArrayList<String>()
        folders.forEach { if (it.inAssetsAndWithContent(this)) actualFolders.add(it) }
        
        var count = 0
        
        actualFolders.forEachIndexed { index, s ->
            destroyDialog()
            val dialogContent = string(
                    R.string.copying_assets,
                    CopyAssetsTask.getCorrectFolderName(s))
            dialog = buildMaterialDialog {
                content(dialogContent.orEmpty())
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
                                string(
                                        if (count == actualFolders.size) R.string.copied_assets_successfully
                                        else R.string.copied_assets_error),
                                Snackbar.LENGTH_LONG)
                        if (count == actualFolders.size) {
                            (getCurrentFragment() as? SetupFragment)?.loadDataFromViewModel() ?: {
                                ((pager?.adapter as? FragmentsPagerAdapter)?.get(
                                        currentItemId) as? SetupFragment)?.loadDataFromViewModel()
                            }()
                        }
                    }
                }).execute()
            }
            dialog?.show()
        }
    }
}