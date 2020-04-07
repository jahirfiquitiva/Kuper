package dev.jahir.kuper.data.viewmodels

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.utils.lazyMutableLiveData
import dev.jahir.kuper.R
import dev.jahir.kuper.data.KLCK_PACKAGE
import dev.jahir.kuper.data.KLWP_PACKAGE
import dev.jahir.kuper.data.KOLORETTE_PACKAGE
import dev.jahir.kuper.data.KWGT_PACKAGE
import dev.jahir.kuper.data.MEDIA_UTILS_PACKAGE
import dev.jahir.kuper.data.ZOOPER_PACKAGE
import dev.jahir.kuper.data.models.RequiredApp
import dev.jahir.kuper.data.tasks.KuperAssets
import dev.jahir.kuper.extensions.isAppInstalled
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

            val hasTemplates = KuperAssets.hasAssets(context, "templates")
            val hasWidgets = KuperAssets.hasAssets(context, "widgets")
            val hasWallpapers = KuperAssets.hasAssets(context, "wallpapers")
            val hasLockScreens = KuperAssets.hasAssets(context, "lockscreens")

            if (!context.isAppInstalled(ZOOPER_PACKAGE) && hasTemplates) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.zooper_widget),
                        context.string(R.string.required_for_widgets),
                        R.drawable.ic_zooper, ZOOPER_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled(KWGT_PACKAGE) && hasWidgets) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.kwgt),
                        context.string(R.string.required_for_widgets),
                        R.drawable.ic_kustom, KWGT_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled("$KWGT_PACKAGE.pro") && hasWidgets) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.kwgt_pro),
                        context.string(R.string.required_for_widgets),
                        R.drawable.ic_kustom, "$KWGT_PACKAGE.pro"
                    )
                )
            }

            if (!context.isAppInstalled(KLWP_PACKAGE) && hasWallpapers) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klwp),
                        context.string(R.string.required_for_wallpapers),
                        R.drawable.ic_kustom, KLWP_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled("$KLWP_PACKAGE.pro") && hasWallpapers) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klwp_pro),
                        context.string(R.string.required_for_wallpapers),
                        R.drawable.ic_kustom, "$KLWP_PACKAGE.pro"
                    )
                )
            }

            if (!context.isAppInstalled(KLCK_PACKAGE) && hasLockScreens) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klck),
                        context.string(R.string.required_for_lockscreens),
                        R.drawable.ic_kustom, KLCK_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled("$KLCK_PACKAGE.pro") && hasLockScreens) {
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

            if (!KuperAssets.areZooperAssetsInstalled(context)) {
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
}