package dev.jahir.kuper.ui.fragments

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import dev.jahir.frames.extensions.dimenPixelSize
import dev.jahir.frames.extensions.dpToPx
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.integer
import dev.jahir.frames.extensions.lower
import dev.jahir.frames.extensions.mdDialog
import dev.jahir.frames.extensions.openLink
import dev.jahir.frames.extensions.toast
import dev.jahir.frames.ui.activities.base.BaseLicenseCheckerActivity.Companion.PLAY_STORE_LINK_PREFIX
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.kuper.R
import dev.jahir.kuper.data.models.Component
import dev.jahir.kuper.extensions.hasStoragePermission
import dev.jahir.kuper.ui.activities.KuperActivity
import dev.jahir.kuper.ui.adapters.ComponentsAdapter
import dev.jahir.kuper.ui.decorations.SectionedGridSpacingDecoration
import dev.jahir.kuper.utils.KLCK_PACKAGE
import dev.jahir.kuper.utils.KLWP_PACKAGE
import dev.jahir.kuper.utils.KWGT_PACKAGE
import dev.jahir.kuper.utils.ZOOPER_PACKAGE

@SuppressLint("MissingPermission")
class ComponentsFragment : BaseFramesFragment<Component>() {

    private val componentsAdapter: ComponentsAdapter by lazy { ComponentsAdapter(::onClick) }

    private val wallpaper: Drawable?
        get() = activity?.let {
            try {
                val wm = WallpaperManager.getInstance(it)
                if (it.hasStoragePermission) wm?.fastDrawable else null
            } catch (e: Exception) {
                null
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        componentsAdapter.updateSectionTitles(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val columnsCount = context?.integer(R.integer.wallpapers_columns_count, 2) ?: 2
        val gridLayoutManager =
            GridLayoutManager(context, columnsCount, GridLayoutManager.VERTICAL, false)
        recyclerView?.layoutManager = gridLayoutManager
        recyclerView?.addItemDecoration(
            SectionedGridSpacingDecoration(
                columnsCount,
                context?.dimenPixelSize(R.dimen.grids_spacing, 8.dpToPx) ?: 8.dpToPx
            )
        )
        componentsAdapter.setLayoutManager(gridLayoutManager)
        componentsAdapter.wallpaper = wallpaper
        recyclerView?.adapter = componentsAdapter
        loadData()
    }

    override fun onResume() {
        super.onResume()
        updateDeviceWallpaper()
    }

    internal fun updateDeviceWallpaper() {
        try {
            componentsAdapter.wallpaper = wallpaper
        } catch (e: Exception) {
        }
    }

    override fun loadData() {
        (activity as? KuperActivity)?.loadComponents()
    }

    private fun onClick(component: Component) {
        context?.let { contxt ->
            component.getIntent(contxt)?.let {
                try {
                    startActivity(it)
                } catch (e: Exception) {
                    val itemPkg = when (component.type) {
                        Component.Type.ZOOPER -> ZOOPER_PACKAGE
                        Component.Type.WALLPAPER -> KLWP_PACKAGE
                        Component.Type.WIDGET -> KWGT_PACKAGE
                        Component.Type.LOCKSCREEN -> KLCK_PACKAGE
                        else -> ""
                    }
                    if (itemPkg.hasContent()) {
                        contxt.toast(R.string.app_not_installed)
                        contxt.openLink(PLAY_STORE_LINK_PREFIX + itemPkg)
                    }
                }
            } ?: {
                if (component.type == Component.Type.KOMPONENT) {
                    activity?.mdDialog {
                        setTitle(R.string.komponents)
                        setMessage(R.string.open_komponents)
                        setPositiveButton(android.R.string.ok) { _, _ -> }
                    }?.show()
                }
            }()
        }
    }

    override fun getFilteredItems(
        originalItems: ArrayList<Component>,
        filter: String
    ): ArrayList<Component> =
        ArrayList(originalItems.filter { it.name.lower().contains(filter.lower()) })

    override fun updateItemsInAdapter(items: ArrayList<Component>) {
        componentsAdapter.components = items
        recyclerView?.loading = false
    }

    companion object {
        internal const val TAG = "ComponentsFragment"

        @JvmStatic
        fun create(components: ArrayList<Component> = ArrayList()): ComponentsFragment =
            ComponentsFragment().apply { updateItems(components) }
    }
}