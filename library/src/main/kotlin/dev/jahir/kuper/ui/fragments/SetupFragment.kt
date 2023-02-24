package dev.jahir.kuper.ui.fragments

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.extensions.context.openLink
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.ui.activities.base.BaseLicenseCheckerActivity.Companion.PLAY_STORE_LINK_PREFIX
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.kuper.data.models.RequiredApp
import dev.jahir.kuper.ui.activities.KuperActivity
import dev.jahir.kuper.ui.adapters.RequiredAppsAdapter


class SetupFragment : BaseFramesFragment<RequiredApp>() {

    private val dividerDecoration by lazy {
        object : DividerItemDecoration(context, DividerItemDecoration.VERTICAL) {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position == state.itemCount - 1) {
                    outRect.setEmpty()
                    outRect.set(0, 0, 0, 0)
                } else super.getItemOffsets(outRect, view, parent, state)
            }
        }
    }

    private val requiredAppsAdapter: RequiredAppsAdapter by lazy {
        RequiredAppsAdapter(::onClick)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView?.setFastScrollEnabled(false)
        recyclerView?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView?.addItemDecoration(dividerDecoration)
        recyclerView?.adapter = requiredAppsAdapter
    }

    override fun onResume() {
        super.onResume()
        cleanRecyclerViewState()
    }

    override fun loadData() {
        (activity as? KuperActivity)?.loadRequiredApps()
    }

    private fun onClick(requiredApp: RequiredApp) {
        if (requiredApp.packageName.hasContent())
            context?.openLink(PLAY_STORE_LINK_PREFIX + requiredApp.packageName)
    }

    override fun getFilteredItems(
        originalItems: ArrayList<RequiredApp>,
        filter: String
    ): ArrayList<RequiredApp> = originalItems

    override fun updateItemsInAdapter(items: List<RequiredApp>) {
        requiredAppsAdapter.apps = items
        cleanRecyclerViewState()
    }

    internal fun cleanRecyclerViewState() {
        recyclerView?.apply {
            allowFirstRunCheck = false
            searching = false
            loading = false
        }
    }

    companion object {
        const val TAG = "required_apps_fragment"

        @JvmStatic
        fun create(requiredApps: ArrayList<RequiredApp> = ArrayList()): SetupFragment =
            SetupFragment().apply { updateItemsInAdapter(requiredApps) }
    }
}
