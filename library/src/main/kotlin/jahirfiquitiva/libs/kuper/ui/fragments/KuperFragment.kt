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

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.View
import ca.allanwang.kau.utils.dimenPixelSize
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.setPaddingBottom
import ca.allanwang.kau.utils.startLink
import com.bumptech.glide.Glide
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.archhelpers.extensions.lazyViewModel
import jahirfiquitiva.libs.archhelpers.ui.fragments.ViewModelFragment
import jahirfiquitiva.libs.frames.helpers.extensions.buildMaterialDialog
import jahirfiquitiva.libs.frames.helpers.extensions.jfilter
import jahirfiquitiva.libs.frames.helpers.extensions.tilesColor
import jahirfiquitiva.libs.frames.helpers.utils.PLAY_STORE_LINK_PREFIX
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kauextensions.extensions.actv
import jahirfiquitiva.libs.kauextensions.extensions.ctxt
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.isInPortraitMode
import jahirfiquitiva.libs.kauextensions.extensions.isLowRamDevice
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.data.models.KuperKomponent
import jahirfiquitiva.libs.kuper.helpers.utils.KLCK_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KLWP_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KWGT_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.ZOOPER_PACKAGE
import jahirfiquitiva.libs.kuper.providers.viewmodels.KuperViewModel
import jahirfiquitiva.libs.kuper.ui.activities.KuperActivity
import jahirfiquitiva.libs.kuper.ui.adapters.KuperAdapter
import jahirfiquitiva.libs.kuper.ui.decorations.SectionedGridSpacingDecoration
import java.lang.ref.WeakReference

@Suppress("DEPRECATION")
class KuperFragment : ViewModelFragment<KuperKomponent>() {
    
    private val kuperViewModel: KuperViewModel by lazyViewModel()
    
    private var recyclerView: EmptyViewRecyclerView? = null
    private var fastScroller: RecyclerFastScroller? = null
    private var kuperAdapter: KuperAdapter? = null
    
    fun scrollToTop() {
        recyclerView?.post { recyclerView?.scrollToPosition(0) }
    }
    
    @SuppressLint("MissingPermission")
    override fun initUI(content: View) {
        recyclerView = content.findViewById(R.id.list_rv)
        fastScroller = content.findViewById(R.id.fast_scroller)
        fastScroller?.setPaddingBottom(48.dpToPx)
        
        recyclerView?.let {
            with(it) {
                itemAnimator = if (context.isLowRamDevice) null else DefaultItemAnimator()
                textView = content.findViewById(R.id.empty_text)
                emptyView = content.findViewById(R.id.empty_view)
                setEmptyImage(R.drawable.empty_section)
                setEmptyText(R.string.empty_section)
                loadingView = content.findViewById(R.id.loading_view)
                setLoadingText(R.string.loading_section)
                
                val spanCount = if (context.isInPortraitMode) 2 else 3
                val gridLayout = object : GridLayoutManager(
                        context, spanCount,
                        GridLayoutManager.VERTICAL,
                        false) {
                    override fun supportsPredictiveItemAnimations(): Boolean = false
                }
                if (activity is KuperActivity) {
                    val wm = WallpaperManager.getInstance(context)
                    
                    val drawable = try {
                        wm?.fastDrawable ?: ColorDrawable(context.tilesColor)
                    } catch (e: Exception) {
                        ColorDrawable(context.tilesColor)
                    }
                    
                    kuperAdapter =
                            KuperAdapter(WeakReference(context), Glide.with(context), drawable) {
                                launchIntentFor(it)
                            }
                    kuperAdapter?.setLayoutManager(gridLayout)
                    
                    layoutManager = gridLayout
                    
                    addItemDecoration(
                            SectionedGridSpacingDecoration(
                                    spanCount,
                                    context.dimenPixelSize(R.dimen.wallpapers_grid_spacing), true,
                                    kuperAdapter))
                    adapter = kuperAdapter
                }
                
                setPaddingBottom(64.dpToPx)
                
                fastScroller?.attachRecyclerView(this)
            }
        }
        
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
        loadDataFromViewModel()
    }
    
    fun applyFilter(filter: String = "") {
        if (filter.hasContent()) {
            recyclerView?.setEmptyImage(R.drawable.no_results)
            recyclerView?.setEmptyText(R.string.search_no_results)
            kuperAdapter?.setItems(ArrayList(kuperViewModel.getData().orEmpty()).jfilter {
                it.name.contains(filter, true) || it.type.toString().contains(filter, true)
            })
        } else {
            recyclerView?.setEmptyImage(R.drawable.empty_section)
            recyclerView?.setEmptyText(R.string.empty_section)
            kuperAdapter?.setItems(ArrayList(kuperViewModel.getData().orEmpty()))
        }
        scrollToTop()
    }
    
    override fun getContentLayout(): Int = R.layout.section_layout
    override fun onItemClicked(item: KuperKomponent, longClick: Boolean) {}
    
    private fun launchIntentFor(item: KuperKomponent) {
        context?.let { contxt ->
            item.getIntent(contxt)?.let {
                try {
                    startActivity(it)
                } catch (e: Exception) {
                    val itemPkg = when (item.type) {
                        KuperKomponent.Type.ZOOPER -> ZOOPER_PACKAGE
                        KuperKomponent.Type.WALLPAPER -> KLWP_PACKAGE
                        KuperKomponent.Type.WIDGET -> KWGT_PACKAGE
                        KuperKomponent.Type.LOCKSCREEN -> KLCK_PACKAGE
                        else -> ""
                    }
                    if (itemPkg.hasContent())
                        contxt.startLink(PLAY_STORE_LINK_PREFIX + itemPkg)
                }
            } ?: {
                if (item.type == KuperKomponent.Type.KOMPONENT) {
                    contxt.buildMaterialDialog {
                        title(R.string.komponents)
                        content(R.string.open_komponents)
                        positiveText(android.R.string.ok)
                    }.show()
                }
            }()
        }
    }
    
    override fun loadDataFromViewModel() {
        kuperViewModel.loadData(ctxt, false)
    }
    
    override fun autoStartLoad() = true
    
    override fun registerObservers() {
        kuperViewModel.observe(this) {
            kuperAdapter?.setItems(it)
            (actv as? KuperActivity)?.destroyDialog()
        }
    }
    
    override fun unregisterObservers() {
        kuperViewModel.destroy(this)
    }
    
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && !allowReloadAfterVisibleToUser()) recyclerView?.updateEmptyState()
    }
}