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
package jahirfiquitiva.libs.kuper.ui.fragments

import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.View
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.openLink
import ca.allanwang.kau.utils.setPaddingBottom
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.archhelpers.extensions.lazyViewModel
import jahirfiquitiva.libs.archhelpers.ui.fragments.ViewModelFragment
import jahirfiquitiva.libs.frames.helpers.utils.PLAY_STORE_LINK_PREFIX
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kext.extensions.context
import jahirfiquitiva.libs.kext.extensions.ctxt
import jahirfiquitiva.libs.kext.extensions.getAppName
import jahirfiquitiva.libs.kext.extensions.hasContent
import jahirfiquitiva.libs.kext.extensions.isInPortraitMode
import jahirfiquitiva.libs.kext.extensions.isLowRamDevice
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.providers.viewmodels.SetupViewModel
import jahirfiquitiva.libs.kuper.ui.activities.KuperActivity
import jahirfiquitiva.libs.kuper.ui.adapters.KuperApp
import jahirfiquitiva.libs.kuper.ui.adapters.SetupAdapter
import java.lang.ref.WeakReference

@Suppress("DEPRECATION")
class SetupFragment : ViewModelFragment<KuperApp>() {
    
    private val appsModel: SetupViewModel by lazyViewModel()
    
    private var recyclerView: EmptyViewRecyclerView? = null
    private var fastScroller: RecyclerFastScroller? = null
    
    private var setupAdapter: SetupAdapter? = null
    
    fun scrollToTop() {
        recyclerView?.post { recyclerView?.scrollToPosition(0) }
    }
    
    override fun initUI(content: View) {
        recyclerView = content.findViewById(R.id.list_rv)
        fastScroller = content.findViewById(R.id.fast_scroller)
        fastScroller?.setPaddingBottom(48.dpToPx)
        
        recyclerView?.let { recyclerView ->
            with(recyclerView) {
                itemAnimator = if (ctxt.isLowRamDevice) null else DefaultItemAnimator()
                textView = content.findViewById(R.id.empty_text)
                emptyView = content.findViewById(R.id.empty_view)
                setEmptyImage(R.drawable.empty_section)
                setEmptyText(R.string.empty_section)
                loadingView = content.findViewById(R.id.loading_view)
                setLoadingText(R.string.loading_section)
                
                val layoutManager = GridLayoutManager(
                    context, if (ctxt.isInPortraitMode) 1 else 2,
                    GridLayoutManager.VERTICAL, false)
                
                setupAdapter = SetupAdapter(WeakReference(ctxt)) { onItemClicked(it, false) }
                setupAdapter?.setLayoutManager(layoutManager)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = setupAdapter
                
                setPaddingBottom(64.dpToPx)
            }
            
            fastScroller?.attachRecyclerView(recyclerView)
            
            recyclerView.state = EmptyViewRecyclerView.State.LOADING
        }
        
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
        loadDataFromViewModel()
    }
    
    override fun getContentLayout(): Int = R.layout.section_layout
    
    override fun onItemClicked(item: KuperApp, longClick: Boolean) {
        if (item.packageName.hasContent()) {
            context { it.openLink(PLAY_STORE_LINK_PREFIX + item.packageName) }
        } else {
            (activity as? KuperActivity)?.let { actv ->
                actv.requestStoragePermission(
                    getString(R.string.permission_request_assets, actv.getAppName())) {
                    actv.installAssets()
                }
            }
        }
    }
    
    override fun loadDataFromViewModel() {
        appsModel.loadData(ctxt, true)
    }
    
    override fun autoStartLoad() = true
    
    override fun registerObservers() {
        appsModel.observe(
            this, {
            if (it.isEmpty()) {
                (activity as? KuperActivity)?.hideSetup()
            } else {
                setupAdapter?.updateApps(it)
            }
        })
    }
    
    override fun unregisterObservers() {
        appsModel.destroy(this)
    }
    
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && !allowReloadAfterVisibleToUser()) recyclerView?.updateEmptyState()
    }
}