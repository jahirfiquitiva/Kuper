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
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import ca.allanwang.kau.utils.isAppInstalled
import ca.allanwang.kau.utils.visible
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import jahirfiquitiva.libs.frames.helpers.extensions.PermissionRequestListener
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.checkPermission
import jahirfiquitiva.libs.frames.helpers.extensions.requestPermissions
import jahirfiquitiva.libs.frames.ui.activities.base.BaseFramesActivity
import jahirfiquitiva.libs.frames.ui.widgets.CustomToolbar
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.inactiveIconsColor
import jahirfiquitiva.libs.kauextensions.extensions.printDebug
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.data.models.FragmentWithKey
import jahirfiquitiva.libs.kuper.data.models.KuperKomponent
import jahirfiquitiva.libs.kuper.helpers.utils.CopyAssetsTask
import jahirfiquitiva.libs.kuper.helpers.utils.CopyAssetsTask.Companion.filesToIgnore
import jahirfiquitiva.libs.kuper.helpers.utils.CopyAssetsTask.Companion.getCorrectFolderName
import jahirfiquitiva.libs.kuper.helpers.utils.KLWP_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KOLORETTE_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KWGT_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.MEDIA_UTILS_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.ZOOPER_PACKAGE
import jahirfiquitiva.libs.kuper.providers.viewmodels.KuperViewModel
import jahirfiquitiva.libs.kuper.ui.adapters.KuperApp
import jahirfiquitiva.libs.kuper.ui.fragments.KuperFragment
import jahirfiquitiva.libs.kuper.ui.fragments.SetupFragment
import jahirfiquitiva.libs.kuper.ui.fragments.WallpapersFragment
import java.io.File
import java.lang.ref.WeakReference

abstract class KuperActivity:BaseFramesActivity() {
    
    private val SETUP_KEY = "setup"
    private val WIDGETS_KEY = "widgets"
    private val WALLPAPERS_KEY = "wallpapers"
    
    private val toolbar:CustomToolbar by bind(R.id.toolbar)
    private val bottomNavigation:AHBottomNavigation by bind(R.id.bottom_navigation)
    
    val apps = ArrayList<KuperApp>()
    val komponents = ArrayList<KuperKomponent>()
    private val fragments = ArrayList<FragmentWithKey>()
    
    private lateinit var kuperViewModel:KuperViewModel
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kuper)
        toolbar.bindToActivity(this, false)
        
        kuperViewModel = ViewModelProviders.of(this).get(KuperViewModel::class.java)
        kuperViewModel.observe(this, { list ->
            komponents.clear()
            komponents.addAll(list)
            destroyDialog()
        })
        kuperViewModel.loadData(this)
        
        setupContent()
    }
    
    private fun setupContent() {
        destroyDialog()
        dialog = buildMaterialDialog {
            content(R.string.loading)
            progress(true, 0)
            cancelable(false)
        }
        dialog?.show()
        
        setupApps()
    }
    
    override fun fragmentsContainer():Int = R.id.fragments_container
    
    private fun setupApps() {
        apps.clear()
        
        if (!isAppInstalled(ZOOPER_PACKAGE)) {
            apps.add(KuperApp(getString(R.string.zooper_widget),
                              getString(R.string.required_for_widgets),
                              "ic_zooper", ZOOPER_PACKAGE))
        }
        
        if (!isAppInstalled(MEDIA_UTILS_PACKAGE) && getBoolean(R.bool.media_utils_required)) {
            apps.add(KuperApp(getString(R.string.media_utils),
                              getString(R.string.required_for_widgets),
                              "ic_zooper", MEDIA_UTILS_PACKAGE))
        }
        
        if (!isAppInstalled(KOLORETTE_PACKAGE) && getBoolean(R.bool.kolorette_required)) {
            apps.add(KuperApp(getString(R.string.kolorette),
                              getString(R.string.required_for_widgets),
                              "ic_zooper", KOLORETTE_PACKAGE))
        }
        
        if (!isAppInstalled(KWGT_PACKAGE) && inAssetsAndWithContent("widgets")) {
            apps.add(KuperApp(getString(R.string.kwgt),
                              getString(R.string.required_for_widgets),
                              "ic_kustom", KWGT_PACKAGE))
        }
        
        if (!isAppInstalled(KLWP_PACKAGE) && inAssetsAndWithContent("wallpapers")) {
            apps.add(KuperApp(getString(R.string.klwp),
                              getString(R.string.required_for_wallpapers),
                              "ic_kustom", KLWP_PACKAGE))
        }
        
        if (!areAssetsInstalled()) {
            apps.add(KuperApp(getString(R.string.zooper_widget),
                              getString(R.string.required_assets),
                              "ic_zooper"))
        }
        
        apps.forEach { printDebug("Found app: $it") }
        
        setupBottomNavigation()
    }
    
    private fun setupBottomNavigation() {
        fragments.clear()
        
        if (apps.isNotEmpty())
            fragments.add(FragmentWithKey(SETUP_KEY, SetupFragment()))
        
        fragments.add(FragmentWithKey(WIDGETS_KEY, KuperFragment()))
        fragments.add(FragmentWithKey(WALLPAPERS_KEY, WallpapersFragment()))
        
        fragments.forEach { printDebug("Added fragment with key ${it.key}") }
        
        bottomNavigation.accentColor = accentColor
        with(bottomNavigation) {
            defaultBackgroundColor = cardBackgroundColor
            inactiveColor = inactiveIconsColor
            // TODO: Enable this?
            // isBehaviorTranslationEnabled = false
            isForceTint = true
            titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
            
            removeAllItems()
            
            if (apps.isNotEmpty())
                addItem(AHBottomNavigationItem(getString(R.string.setup), R.drawable.ic_setup))
            
            addItem(AHBottomNavigationItem(getString(R.string.widgets), R.drawable.ic_widgets))
            addItem(AHBottomNavigationItem(getString(R.string.wallpapers),
                                           R.drawable.ic_all_wallpapers))
            
            setOnTabSelectedListener { position, _ ->
                changeFragment(fragments[position].fragment)
                true
            }
            setCurrentItem(0, true)
            visible()
        }
    }
    
    private fun inAssetsAndWithContent(folder:String):Boolean {
        val folders = assets.list("")
        return if (folders.contains(folder)) {
            val files = assets.list(folder)
            files?.isNotEmpty() ?: false
        } else false
    }
    
    private fun areAssetsInstalled():Boolean {
        val folders = arrayOf("fonts", "iconsets", "bitmaps")
        
        var count = 0
        
        for (folder in folders) {
            try {
                val files = assets.list(folder)
                files?.forEach {
                    if (it.contains(".") && !(filesToIgnore.contains(it))) {
                        val file = File(
                                "${Environment.getExternalStorageDirectory()}/ZooperWidget/${getCorrectFolderName(
                                        folder)}/$it")
                        if (file.exists()) count += 1
                    }
                }
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
        
        return count == folders.size
    }
    
    override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<out String>,
                                            grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 43) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                installAssets()
            } else {
                showSnackbar(getString(R.string.permission_denied), Snackbar.LENGTH_LONG)
            }
        }
    }
    
    fun requestPermissionInstallAssets() {
        checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                object:PermissionRequestListener {
                    override fun onPermissionRequest(permission:String) =
                            requestPermissions(43, permission)
                    
                    override fun showPermissionInformation(permission:String) =
                            showPermissionExplanation()
                    
                    override fun onPermissionCompletelyDenied() =
                            showSnackbar(getString(R.string.permission_denied_completely),
                                         Snackbar.LENGTH_LONG)
                    
                    override fun onPermissionGranted() = installAssets()
                })
    }
    
    private fun showPermissionExplanation() {
        showSnackbar(getString(R.string.permission_request_assets, getAppName()),
                     Snackbar.LENGTH_LONG, {
                         setAction(R.string.allow, {
                             dismiss()
                             installAssets()
                         })
                     })
    }
    
    fun installAssets() {
        val folders = arrayOf("fonts", "iconsets", "bitmaps")
        var count = 0
        destroyDialog()
        for (folder in folders) {
            val dialogContent = getString(R.string.copying_assets, getCorrectFolderName(folder))
            dialog = buildMaterialDialog {
                content(dialogContent)
                progress(true, 0)
                cancelable(false)
            }
            dialog?.setOnShowListener {
                CopyAssetsTask(WeakReference(this), folder, {
                    if (it) count += 1
                    destroyDialog()
                }).execute()
            }
            dialog?.show()
        }
        showSnackbar(getString(
                if (count == folders.size) R.string.copied_assets_successfully
                else R.string.copied_assets_error),
                     Snackbar.LENGTH_LONG)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        kuperViewModel.destroy(this)
    }
}