package dev.jahir.kuper.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.openLink
import dev.jahir.frames.ui.activities.base.BaseLicenseCheckerActivity.Companion.PLAY_STORE_LINK_PREFIX
import dev.jahir.frames.ui.activities.base.BaseStoragePermissionRequestActivity
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.frames.ui.widgets.EmptyView
import dev.jahir.frames.ui.widgets.EmptyViewRecyclerView
import dev.jahir.kuper.R
import dev.jahir.kuper.data.models.RequiredApp
import dev.jahir.kuper.ui.activities.KuperActivity
import dev.jahir.kuper.ui.adapters.RequiredAppsAdapter

class SetupFragment : BaseFramesFragment<RequiredApp>() {

    private val requiredAppsAdapter: RequiredAppsAdapter by lazy {
        RequiredAppsAdapter(::onClick)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView?.adapter = requiredAppsAdapter
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
    }

    override fun loadData() {
        (activity as? KuperActivity)?.loadRequiredApps()
    }

    private fun onClick(requiredApp: RequiredApp) {
        if (requiredApp.packageName.hasContent()) {
            context?.openLink(PLAY_STORE_LINK_PREFIX + requiredApp.packageName)
        } else {
            (activity as? BaseStoragePermissionRequestActivity<*>)?.requestStoragePermission()
        }
    }

    override fun onStateChanged(state: EmptyViewRecyclerView.State, emptyView: EmptyView?) {
        super.onStateChanged(state, emptyView)
        if (state == EmptyViewRecyclerView.State.EMPTY) {
            emptyView?.setImageDrawable(R.drawable.ic_empty_section)
            emptyView?.setEmpty(context?.getString(R.string.empty_section) ?: "")
        }
    }

    override fun getFilteredItems(
        originalItems: ArrayList<RequiredApp>,
        filter: String,
        closed: Boolean
    ): ArrayList<RequiredApp> = originalItems

    override fun updateItemsInAdapter(items: ArrayList<RequiredApp>) {
        requiredAppsAdapter.apps = items
    }

    companion object {
        internal const val TAG = "RequiredAppsFragment"

        @JvmStatic
        fun create(): SetupFragment = SetupFragment()
    }
}