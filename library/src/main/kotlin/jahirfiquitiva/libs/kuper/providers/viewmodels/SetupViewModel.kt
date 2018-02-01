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
package jahirfiquitiva.libs.kuper.providers.viewmodels

import android.content.Context
import android.os.Environment
import ca.allanwang.kau.utils.isAppInstalled
import jahirfiquitiva.libs.archhelpers.viewmodels.ListViewModel
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.helpers.extensions.getFilesInAssetsFolder
import jahirfiquitiva.libs.kuper.helpers.extensions.inAssetsAndWithContent
import jahirfiquitiva.libs.kuper.helpers.utils.CopyAssetsTask
import jahirfiquitiva.libs.kuper.helpers.utils.KLCK_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KLWP_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KOLORETTE_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KWGT_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.MEDIA_UTILS_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.ZOOPER_PACKAGE
import jahirfiquitiva.libs.kuper.ui.adapters.KuperApp
import java.io.File

class SetupViewModel : ListViewModel<Context, KuperApp>() {
    override fun internalLoad(param: Context): ArrayList<KuperApp> {
        val apps = ArrayList<KuperApp>()
        
        if (!param.isAppInstalled(ZOOPER_PACKAGE) && "templates".inAssetsAndWithContent(param)) {
            apps.add(
                    KuperApp(
                            param.getString(R.string.zooper_widget),
                            param.getString(R.string.required_for_widgets),
                            "ic_zooper", ZOOPER_PACKAGE))
        }
        
        if (!param.isAppInstalled(MEDIA_UTILS_PACKAGE) &&
                param.getBoolean(R.bool.media_utils_required)) {
            apps.add(
                    KuperApp(
                            param.getString(R.string.media_utils),
                            param.getString(R.string.required_for_widgets),
                            "ic_zooper", MEDIA_UTILS_PACKAGE))
        }
        
        if (!param.isAppInstalled(KOLORETTE_PACKAGE) &&
                param.getBoolean(R.bool.kolorette_required)) {
            apps.add(
                    KuperApp(
                            param.getString(R.string.kolorette),
                            param.getString(R.string.required_for_widgets),
                            "ic_zooper", KOLORETTE_PACKAGE))
        }
        
        if (!param.isAppInstalled(KWGT_PACKAGE) && "widgets".inAssetsAndWithContent(param)) {
            apps.add(
                    KuperApp(
                            param.getString(R.string.kwgt),
                            param.getString(R.string.required_for_widgets),
                            "ic_kustom", KWGT_PACKAGE))
        }
        
        if (!param.isAppInstalled("$KWGT_PACKAGE.pro") && "widgets".inAssetsAndWithContent(param)) {
            apps.add(
                    KuperApp(
                            param.getString(R.string.kwgt_pro),
                            param.getString(R.string.required_for_widgets),
                            "ic_kustom", "$KWGT_PACKAGE.pro"))
        }
        
        if (!param.isAppInstalled(KLWP_PACKAGE) && "wallpapers".inAssetsAndWithContent(param)) {
            apps.add(
                    KuperApp(
                            param.getString(R.string.klwp),
                            param.getString(R.string.required_for_wallpapers),
                            "ic_kustom", KLWP_PACKAGE))
        }
        
        if (!param.isAppInstalled("$KLWP_PACKAGE.pro") && "wallpapers".inAssetsAndWithContent(
                        param)) {
            apps.add(
                    KuperApp(
                            param.getString(R.string.klwp_pro),
                            param.getString(R.string.required_for_wallpapers),
                            "ic_kustom", "$KLWP_PACKAGE.pro"))
        }
        
        if (!param.isAppInstalled(KLCK_PACKAGE) && "lockscreens".inAssetsAndWithContent(param)) {
            apps.add(
                    KuperApp(
                            param.getString(R.string.klck),
                            param.getString(R.string.required_for_lockscreens),
                            "ic_kustom", KLCK_PACKAGE))
        }
        
        if (!param.isAppInstalled("$KLCK_PACKAGE.pro") &&
                "lockscreens".inAssetsAndWithContent(param)) {
            apps.add(
                    KuperApp(
                            param.getString(R.string.klck_pro),
                            param.getString(R.string.required_for_lockscreens),
                            "ic_kustom", "$KLCK_PACKAGE.pro"))
        }
        
        if (!areAssetsInstalled(param)) {
            apps.add(
                    KuperApp(
                            param.getString(R.string.widgets),
                            param.getString(R.string.required_assets),
                            "ic_zooper"))
        }
        return apps
    }
    
    private fun areAssetsInstalled(context: Context): Boolean {
        val folders = arrayOf("fonts", "iconsets", "bitmaps")
        val actualFolders = ArrayList<String>()
        folders.forEach { if (it.inAssetsAndWithContent(context)) actualFolders.add(it) }
        
        var count = 0
        
        for (folder in actualFolders) {
            var filesCount = 0
            val possibleFiles = folder.getFilesInAssetsFolder(context)
            possibleFiles.forEach {
                if (it.contains(".") && (!CopyAssetsTask.filesToIgnore.contains(it))) {
                    val file = File(
                            "${Environment.getExternalStorageDirectory()}/ZooperWidget/${CopyAssetsTask.getCorrectFolderName(
                                    folder)}/$it")
                    if (file.exists()) filesCount += 1
                }
            }
            if (filesCount == possibleFiles.size) count += 1
        }
        
        return count == actualFolders.size
    }
}