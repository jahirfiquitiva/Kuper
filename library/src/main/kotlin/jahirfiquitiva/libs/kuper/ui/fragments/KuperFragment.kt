/*
 * Copyright (c) 2017. Jahir Fiquitiva
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

import android.app.WallpaperManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.View
import ca.allanwang.kau.utils.dimenPixelSize
import com.bumptech.glide.Glide
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.helpers.extensions.isLowRamDevice
import jahirfiquitiva.libs.frames.helpers.utils.PLAY_STORE_LINK_PREFIX
import jahirfiquitiva.libs.frames.ui.fragments.base.BasicFragment
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kauextensions.extensions.isInPortraitMode
import jahirfiquitiva.libs.kauextensions.extensions.openLink
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.data.models.KuperKomponent
import jahirfiquitiva.libs.kuper.ui.activities.KuperActivity
import jahirfiquitiva.libs.kuper.ui.adapters.KuperAdapter
import jahirfiquitiva.libs.kuper.ui.decorations.SectionedGridSpacingDecoration
import java.lang.ref.WeakReference

class KuperFragment:BasicFragment<KuperKomponent>() {
    
    private lateinit var swipeToRefresh:SwipeRefreshLayout
    private lateinit var rv:EmptyViewRecyclerView
    private lateinit var fastScroll:RecyclerFastScroller
    
    private lateinit var kuperAdapter:KuperAdapter
    
    override fun initUI(content:View) {
        swipeToRefresh = content.findViewById(R.id.swipe_to_refresh)
        swipeToRefresh.isEnabled = false
        rv = content.findViewById(R.id.list_rv)
        fastScroll = content.findViewById(R.id.fast_scroller)
        
        with(rv) {
            itemAnimator = if (context.isLowRamDevice) null else DefaultItemAnimator()
            textView = content.findViewById(R.id.empty_text)
            emptyView = content.findViewById(R.id.empty_view)
            setEmptyImage(R.drawable.empty_section)
            setEmptyText(R.string.empty_section)
            loadingView = content.findViewById(R.id.loading_view)
            setLoadingText(R.string.loading_section)
            
            val spanCount = if (context.isInPortraitMode) 2 else 3
            val layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.VERTICAL,
                                                  false)
            if (activity is KuperActivity) {
                val wm = WallpaperManager.getInstance(context)
                kuperAdapter = KuperAdapter(
                        WeakReference(context), Glide.with(context),
                        wm?.fastDrawable, (activity as KuperActivity).komponents, {
                            try {
                                startActivity(it)
                            } catch (e:Exception) {
                                context.openLink(PLAY_STORE_LINK_PREFIX + it.component.packageName)
                            }
                        })
                kuperAdapter.setLayoutManager(layoutManager)
                rv.layoutManager = layoutManager
                rv.addItemDecoration(SectionedGridSpacingDecoration(spanCount,
                                                                    context.dimenPixelSize(
                                                                            R.dimen.wallpapers_grid_spacing),
                                                                    true, kuperAdapter))
                rv.adapter = kuperAdapter
            }
        }
        
        with(fastScroll) {
            attachSwipeRefreshLayout(swipeToRefresh)
            attachRecyclerView(rv)
        }
        
        rv.state = EmptyViewRecyclerView.State.NORMAL
    }
    
    fun setList(list:ArrayList<KuperKomponent>) {
        kuperAdapter.setList(list)
    }
    
    override fun getContentLayout():Int = R.layout.section_lists
    override fun onItemClicked(item:KuperKomponent) {}
}