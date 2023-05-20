package dev.jahir.kuper.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.utils.context
import dev.jahir.frames.extensions.utils.lazyMutableLiveData
import dev.jahir.frames.extensions.utils.tryToObserve
import dev.jahir.kuper.R
import dev.jahir.kuper.data.KLCK_PACKAGE
import dev.jahir.kuper.data.KLWP_PACKAGE
import dev.jahir.kuper.data.KOLORETTE_PACKAGE
import dev.jahir.kuper.data.KWGT_PACKAGE
import dev.jahir.kuper.data.RENOIR_PACKAGE
import dev.jahir.kuper.data.models.RequiredApp
import dev.jahir.kuper.data.tasks.KuperAssets
import dev.jahir.kuper.extensions.isAppInstalled
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RequiredAppsViewModel(application: Application) : AndroidViewModel(application) {

    private val appsData: MutableLiveData<ArrayList<RequiredApp>> by lazyMutableLiveData()

    val apps: ArrayList<RequiredApp>
        get() = ArrayList(appsData.value.orEmpty())

    fun loadApps() {
        viewModelScope.launch {
            val apps = internalLoadApps()
            appsData.postValue(apps)
        }
    }

    fun observe(owner: LifecycleOwner, onUpdated: (ArrayList<RequiredApp>) -> Unit = {}) {
        appsData.tryToObserve(owner, onUpdated)
    }

    fun destroy(owner: LifecycleOwner) {
        appsData.removeObservers(owner)
    }

    private suspend fun internalLoadApps(): ArrayList<RequiredApp> = withContext(IO) {
        val apps = ArrayList<RequiredApp>()

        val hasWidgets = KuperAssets.hasAssets(context, "widgets")
        val hasWallpapers = KuperAssets.hasAssets(context, "wallpapers")
        val hasLockScreens = KuperAssets.hasAssets(context, "lockscreens")

        if (hasWidgets) {
            if (!context.isAppInstalled(KWGT_PACKAGE)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.kwgt),
                        context.string(R.string.required_for_widgets),
                        R.drawable.ic_kustom, KWGT_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled("$KWGT_PACKAGE.pro")) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.kwgt_pro),
                        context.string(R.string.required_for_widgets),
                        R.drawable.ic_kustom, "$KWGT_PACKAGE.pro"
                    )
                )
            }
        }

        if (hasWallpapers) {
            if (!context.isAppInstalled(KLWP_PACKAGE)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klwp),
                        context.string(R.string.required_for_wallpapers),
                        R.drawable.ic_wallpapers, KLWP_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled("$KLWP_PACKAGE.pro")) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klwp_pro),
                        context.string(R.string.required_for_wallpapers),
                        R.drawable.ic_wallpapers, "$KLWP_PACKAGE.pro"
                    )
                )
            }
        }

        if (hasLockScreens) {
            if (!context.isAppInstalled(KLCK_PACKAGE)) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klck),
                        context.string(R.string.required_for_lockscreens),
                        R.drawable.ic_klck, KLCK_PACKAGE
                    )
                )
            }

            if (!context.isAppInstalled("$KLCK_PACKAGE.pro")) {
                apps.add(
                    RequiredApp(
                        context.string(R.string.klck_pro),
                        context.string(R.string.required_for_lockscreens),
                        R.drawable.ic_klck, "$KLCK_PACKAGE.pro"
                    )
                )
            }
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

        if (!context.isAppInstalled(RENOIR_PACKAGE)
            && context.boolean(R.bool.renoir_required)) {
            apps.add(
                RequiredApp(
                    context.string(R.string.renoir),
                    context.string(R.string.required_for_templates),
                    R.drawable.ic_palette, RENOIR_PACKAGE
                )
            )
        }

        apps
    }
}
