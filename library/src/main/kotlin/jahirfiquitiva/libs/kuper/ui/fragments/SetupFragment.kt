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

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.View
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.setPaddingBottom
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.helpers.utils.PLAY_STORE_LINK_PREFIX
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kauextensions.extensions.ctxt
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isInPortraitMode
import jahirfiquitiva.libs.kauextensions.extensions.isLowRamDevice
import jahirfiquitiva.libs.kauextensions.extensions.openLink
import jahirfiquitiva.libs.kauextensions.ui.fragments.Fragment
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.ui.activities.KuperActivity
import jahirfiquitiva.libs.kuper.ui.adapters.KuperApp
import jahirfiquitiva.libs.kuper.ui.adapters.SetupAdapter
import java.lang.ref.WeakReference

class SetupFragment : Fragment<KuperApp>() {
    
    private lateinit var swipeToRefresh: SwipeRefreshLayout
    private lateinit var rv: EmptyViewRecyclerView
    private lateinit var fastScroll: RecyclerFastScroller
    
    private var setupAdapter: SetupAdapter? = null
    
    override fun initUI(content: View) {
        swipeToRefresh = content.findViewById(R.id.swipe_to_refresh)
        swipeToRefresh.isEnabled = false
        rv = content.findViewById(R.id.list_rv)
        fastScroll = content.findViewById(R.id.fast_scroller)
        
        with(rv) {
            itemAnimator = if (ctxt.isLowRamDevice) null else DefaultItemAnimator()
            textView = content.findViewById(R.id.empty_text)
            emptyView = content.findViewById(R.id.empty_view)
            setEmptyImage(R.drawable.empty_section)
            setEmptyText(R.string.empty_section)
            loadingView = content.findViewById(R.id.loading_view)
            setLoadingText(R.string.loading_section)
            
            updateList()
            setPaddingBottom(64.dpToPx)
        }
        
        with(fastScroll) {
            attachSwipeRefreshLayout(swipeToRefresh)
            attachRecyclerView(rv)
        }
        
        rv.state = EmptyViewRecyclerView.State.NORMAL
    }
    
    fun updateList() {
        if (activity is KuperActivity) {
            val layoutManager = GridLayoutManager(
                    context, if (ctxt.isInPortraitMode) 1 else 2,
                    GridLayoutManager.VERTICAL, false)
            
            setupAdapter = SetupAdapter(
                    WeakReference(ctxt),
                    (activity as KuperActivity).apps, {
                        if (it.packageName.hasContent()) {
                            ctxt.openLink(
                                    PLAY_STORE_LINK_PREFIX + it.packageName)
                        } else {
                            (activity as KuperActivity).requestPermissionInstallAssets()
                        }
                    })
            setupAdapter?.setLayoutManager(layoutManager)
            rv.layoutManager = layoutManager
            rv.adapter = setupAdapter
            rv.state = EmptyViewRecyclerView.State.NORMAL
        }
    }
    
    override fun getContentLayout(): Int = R.layout.section_lists
    override fun onItemClicked(item: KuperApp, longClick: Boolean) {}
}