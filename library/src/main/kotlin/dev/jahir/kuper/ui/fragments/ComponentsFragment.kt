package dev.jahir.kuper.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.fondesa.kpermissions.PermissionStatus
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.data.listeners.BasePermissionRequestListener
import dev.jahir.frames.extensions.context.dimenPixelSize
import dev.jahir.frames.extensions.context.getAppName
import dev.jahir.frames.extensions.context.integer
import dev.jahir.frames.extensions.context.openLink
import dev.jahir.frames.extensions.context.toast
import dev.jahir.frames.extensions.fragments.mdDialog
import dev.jahir.frames.extensions.fragments.string
import dev.jahir.frames.extensions.resources.dpToPx
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.resources.lower
import dev.jahir.frames.extensions.utils.lazyViewModel
import dev.jahir.frames.extensions.views.snackbar
import dev.jahir.frames.ui.activities.base.BaseLicenseCheckerActivity.Companion.PLAY_STORE_LINK_PREFIX
import dev.jahir.frames.ui.activities.base.BasePermissionsRequestActivity
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.kuper.R
import dev.jahir.kuper.data.KLCK_PACKAGE
import dev.jahir.kuper.data.KLWP_PACKAGE
import dev.jahir.kuper.data.KWGT_PACKAGE
import dev.jahir.kuper.data.models.Component
import dev.jahir.kuper.data.viewmodels.ComponentsViewModel
import dev.jahir.kuper.extensions.userWallpaper
import dev.jahir.kuper.ui.adapters.ComponentsAdapter
import dev.jahir.kuper.ui.decorations.SectionedGridSpacingDecoration

@SuppressLint("MissingPermission")
class ComponentsFragment : BaseFramesFragment<Component>() {

    private val componentsViewModel: ComponentsViewModel by lazyViewModel()
    private val componentsAdapter: ComponentsAdapter by lazy { ComponentsAdapter(::onClick) }

    private val wallpaper: Drawable?
        get() = activity?.userWallpaper

    private fun requestStoragePermission() {
        permissionsBuilder(Manifest.permission.READ_EXTERNAL_STORAGE)
            .build()
            .apply {
                addListener(object : BasePermissionRequestListener {
                    override fun onPermissionsGranted(result: List<PermissionStatus>) {
                        super.onPermissionsGranted(result)
                        componentsAdapter.wallpaper = wallpaper
                    }

                    override fun onPermissionsShouldShowRationale(result: List<PermissionStatus>) {
                        super.onPermissionsShouldShowRationale(result)
                        showPermissionRationale()
                    }
                })
            }
            .send()
    }

    private fun showPermissionRationale() {
        snackbar(
            string(R.string.permission_request, context?.getAppName()),
            Snackbar.LENGTH_INDEFINITE,
            (activity as? BasePermissionsRequestActivity<*>)?.snackbarAnchorId ?: 0
        ) {
            setAction(android.R.string.ok) {
                requestStoragePermission()
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        componentsViewModel.observe(this) { updateItems(it) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        componentsAdapter.updateSectionTitles(context)
        requestStoragePermission()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView?.setFastScrollEnabled(false)
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

    override fun onDestroy() {
        super.onDestroy()
        componentsViewModel.destroy(this)
    }

    internal fun updateDeviceWallpaper() {
        try {
            componentsAdapter.wallpaper = wallpaper
        } catch (e: Exception) {
        }
    }

    override fun loadData() {
        componentsViewModel.loadComponents()
    }

    private fun onClick(component: Component) {
        context?.let { contxt ->
            component.getIntent(contxt)?.let {
                try {
                    startActivity(it)
                } catch (e: Exception) {
                    val itemPkg = when (component.type) {
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
            } ?: run {
                if (component.type == Component.Type.KOMPONENT) {
                    activity?.mdDialog {
                        setTitle(R.string.komponents)
                        setMessage(R.string.open_komponents)
                        setPositiveButton(android.R.string.ok) { _, _ -> }
                    }?.show()
                }
            }
        }
    }

    override fun getFilteredItems(
        originalItems: ArrayList<Component>,
        filter: String
    ): ArrayList<Component> =
        ArrayList(originalItems.filter { it.name.lower().contains(filter.lower()) })

    override fun updateItemsInAdapter(items: List<Component>) {
        componentsAdapter.components = items
        recyclerView?.loading = false
    }

    companion object {
        const val TAG = "components_fragment"
    }
}
