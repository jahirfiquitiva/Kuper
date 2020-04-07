package dev.jahir.kuper.data.viewmodels

import android.content.Context
import android.os.Environment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import dev.jahir.frames.extensions.boolean
import dev.jahir.frames.extensions.lazyMutableLiveData
import dev.jahir.frames.extensions.string
import dev.jahir.kuper.R
import dev.jahir.kuper.data.models.RequiredApp
import dev.jahir.kuper.extensions.getFilesInAssetsFolder
import dev.jahir.kuper.extensions.inAssetsAndWithContent
import dev.jahir.kuper.extensions.isAppInstalled
import dev.jahir.kuper.utils.CopyAssetsTask
import dev.jahir.kuper.utils.KLCK_PACKAGE
import dev.jahir.kuper.utils.KLWP_PACKAGE
import dev.jahir.kuper.utils.KOLORETTE_PACKAGE
import dev.jahir.kuper.utils.KWGT_PACKAGE
import dev.jahir.kuper.utils.MEDIA_UTILS_PACKAGE
import dev.jahir.kuper.utils.ZOOPER_PACKAGE
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RequiredAppsViewModel : ViewModel() {

    private val appsData: MutableLiveData<ArrayList<RequiredApp>> by lazyMutableLiveData()

    val apps: ArrayList<RequiredApp>
        get() = ArrayList(appsData.value.orEmpty())

    fun loadApps(context: Context) {
        viewModelScope.launch {
            val apps = internalLoadApps(context)
            appsData.postValue(apps)
        }
    }

    fun observe(owner: LifecycleOwner, onUpdated: (ArrayList<RequiredApp>) -> Unit = {}) {
        appsData.observe(owner) { onUpdated(it) }
    }

    fun destroy(owner: LifecycleOwner) {
        appsData.removeObservers(owner)
    }

    private suspend fun internalLoadApps(context: Context): ArrayList<RequiredApp> =
        withContext(IO) {
            val apps = ArrayList<RequiredApp>()

            if (!context.isAppInstalled(ZOOPER_PACKAGE)
                && "templates".inAssetsAndWithContent(context)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.zooper_widget),
                        context.string(R.string.required_for_widgets),
                        R.drawable.ic_zooper, ZOOPER_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled(KWGT_PACKAGE)
                && "widgets".inAssetsAndWithContent(context)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.kwgt),
                        context.string(R.string.required_for_widgets),
                        R.drawable.ic_kustom, KWGT_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled("$KWGT_PACKAGE.pro")
                && "widgets".inAssetsAndWithContent(context)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.kwgt_pro),
                        context.string(R.string.required_for_widgets),
                        R.drawable.ic_kustom, "$KWGT_PACKAGE.pro"
                    )
                )
            }

            if (!context.isAppInstalled(KLWP_PACKAGE) && "wallpapers".inAssetsAndWithContent(context)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klwp),
                        context.string(R.string.required_for_wallpapers),
                        R.drawable.ic_kustom, KLWP_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled("$KLWP_PACKAGE.pro")
                && "wallpapers".inAssetsAndWithContent(context)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klwp_pro),
                        context.string(R.string.required_for_wallpapers),
                        R.drawable.ic_kustom, "$KLWP_PACKAGE.pro"
                    )
                )
            }

            if (!context.isAppInstalled(KLCK_PACKAGE)
                && "lockscreens".inAssetsAndWithContent(context)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klck),
                        context.string(R.string.required_for_lockscreens),
                        R.drawable.ic_kustom, KLCK_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled("$KLCK_PACKAGE.pro")
                && "lockscreens".inAssetsAndWithContent(context)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klck_pro),
                        context.string(R.string.required_for_lockscreens),
                        R.drawable.ic_kustom, "$KLCK_PACKAGE.pro"
                    )
                )
            }

            if (!context.isAppInstalled(MEDIA_UTILS_PACKAGE)
                && context.boolean(R.bool.media_utils_required)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.media_utils),
                        context.string(R.string.required_for_widgets),
                        R.drawable.ic_zooper, MEDIA_UTILS_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled(KOLORETTE_PACKAGE)
                && context.boolean(R.bool.kolorette_required)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.kolorette),
                        context.string(R.string.required_for_templates),
                        R.drawable.ic_palette, KOLORETTE_PACKAGE
                    )
                )
            }

            if (!areAssetsInstalled(context)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.widgets),
                        context.string(R.string.required_assets),
                        R.drawable.ic_zooper
                    )
                )
            }
            apps
        }

    @Suppress("DEPRECATION")
    private fun areAssetsInstalled(context: Context): Boolean {
        val folders = arrayOf("fonts", "iconsets", "bitmaps")
        val actualFolders = ArrayList<String>()
        folders.forEach { if (it.inAssetsAndWithContent(context)) actualFolders.add(it) }

        var count = 0

        for (folder in actualFolders) {
            var filesCount = 0
            val possibleFiles = folder.getFilesInAssetsFolder(context)
            possibleFiles.forEach {
                if (it.contains(".") && !CopyAssetsTask.filesToIgnore.contains(it)) {
                    val file = File(
                        "${Environment.getExternalStorageDirectory()}/ZooperWidget/" +
                                "${CopyAssetsTask.getCorrectFolderName(folder)}/$it"
                    )
                    if (file.exists()) filesCount += 1
                }
            }
            if (filesCount == possibleFiles.size) count += 1
        }

        return count == actualFolders.size
    }
}