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
import android.graphics.drawable.Drawable
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.View
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.openLink
import ca.allanwang.kau.utils.postDelayed
import ca.allanwang.kau.utils.setPaddingBottom
import ca.allanwang.kau.utils.toast
import com.bumptech.glide.Glide
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.archhelpers.extensions.getViewModel
import jahirfiquitiva.libs.archhelpers.ui.fragments.ViewModelFragment
import jahirfiquitiva.libs.frames.helpers.extensions.jfilter
import jahirfiquitiva.libs.frames.helpers.extensions.mdDialog
import jahirfiquitiva.libs.frames.helpers.extensions.tilesColor
import jahirfiquitiva.libs.frames.helpers.utils.PLAY_STORE_LINK_PREFIX
import jahirfiquitiva.libs.frames.ui.widgets.EmptyViewRecyclerView
import jahirfiquitiva.libs.kext.extensions.dimenPixelSize
import jahirfiquitiva.libs.kext.extensions.hasContent
import jahirfiquitiva.libs.kext.extensions.int
import jahirfiquitiva.libs.kext.extensions.isLowRamDevice
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.helpers.utils.KL
import jahirfiquitiva.libs.kuper.helpers.utils.KLCK_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KLWP_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KWGT_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.ZOOPER_PACKAGE
import jahirfiquitiva.libs.kuper.models.KuperKomponent
import jahirfiquitiva.libs.kuper.providers.viewmodels.KuperViewModel
import jahirfiquitiva.libs.kuper.ui.activities.KuperActivity
import jahirfiquitiva.libs.kuper.ui.adapters.KuperAdapter
import jahirfiquitiva.libs.kuper.ui.decorations.SectionedGridSpacingDecoration

@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
class KuperFragment : ViewModelFragment<KuperKomponent>() {
    
    private var kuperViewModel: KuperViewModel? = null
    private var recyclerView: EmptyViewRecyclerView? = null
    private var fastScroller: RecyclerFastScroller? = null
    private var kuperAdapter: KuperAdapter? = null
    
    private val wallpaper: Drawable? by lazy {
        activity?.let {
            try {
                val wm = WallpaperManager.getInstance(it)
                wm?.fastDrawable ?: ColorDrawable(it.tilesColor)
            } catch (e: Exception) {
                KL.e(e.message)
                ColorDrawable(it.tilesColor)
            }
        } ?: { null }()
    }
    
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
                
                val spanCount = context.int(R.integer.kuper_previews_columns)
                val gridLayout = object : GridLayoutManager(
                    context, spanCount,
                    GridLayoutManager.VERTICAL,
                    false) {
                    override fun supportsPredictiveItemAnimations(): Boolean = false
                }
                
                kuperAdapter = KuperAdapter(context?.let { Glide.with(it) }) {
                    launchIntentFor(it)
                }
                setupWallpaperInAdapter()
                
                kuperAdapter?.setLayoutManager(gridLayout)
                layoutManager = gridLayout
                
                kuperAdapter?.updateSectionTitles(context)
                addItemDecoration(
                    SectionedGridSpacingDecoration(
                        spanCount,
                        context.dimenPixelSize(R.dimen.wallpapers_grid_spacing), true,
                        kuperAdapter))
                adapter = kuperAdapter
                
                setPaddingBottom(64.dpToPx)
                
                fastScroller?.attachRecyclerView(this)
            }
        }
    }
    
    fun applyFilter(filter: String = "", closed: Boolean = false) {
        if (filter.hasContent()) {
            recyclerView?.setEmptyImage(R.drawable.no_results)
            recyclerView?.setEmptyText(R.string.search_no_results)
            kuperAdapter?.setItems(ArrayList(kuperViewModel?.getData().orEmpty()).jfilter {
                it.name.contains(filter, true) || it.type.toString().contains(filter, true)
            })
        } else {
            recyclerView?.setEmptyImage(R.drawable.empty_section)
            recyclerView?.setEmptyText(R.string.empty_section)
            kuperAdapter?.setItems(ArrayList(kuperViewModel?.getData().orEmpty()))
        }
        if (!closed)
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
                    if (itemPkg.hasContent()) {
                        contxt.toast(contxt.getString(R.string.app_not_installed))
                        contxt.openLink(PLAY_STORE_LINK_PREFIX + itemPkg)
                    }
                }
            } ?: {
                if (item.type == KuperKomponent.Type.KOMPONENT) {
                    contxt.mdDialog {
                        title(R.string.komponents)
                        content(R.string.open_komponents)
                        positiveText(android.R.string.ok)
                    }.show()
                }
            }()
        }
    }
    
    override fun initViewModels() {
        kuperViewModel = getViewModel()
    }
    
    override fun loadDataFromViewModel() {
        activity?.let { kuperViewModel?.loadData(it) }
    }
    
    override fun autoStartLoad() = true
    
    override fun registerObservers() {
        kuperViewModel?.observe(this) {
            recyclerView?.state = EmptyViewRecyclerView.State.NORMAL
            kuperAdapter?.setItems(it)
            (activity as? KuperActivity)?.destroyDialog()
        }
    }
    
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (!allowReloadAfterVisibleToUser()) recyclerView?.updateEmptyState()
            kuperAdapter?.updateSectionTitles(context)
            setupWallpaperInAdapter()
        }
    }
    
    private fun setupWallpaperInAdapter() {
        if (kuperAdapter?.wallpaper == null) {
            try {
                postDelayed(10) { kuperAdapter?.wallpaper = wallpaper }
            } catch (e: Exception) {
                KL.e(e.message)
            }
        }
    }
}