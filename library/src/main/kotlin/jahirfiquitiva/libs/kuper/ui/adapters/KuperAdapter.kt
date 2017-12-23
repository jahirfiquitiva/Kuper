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
package jahirfiquitiva.libs.kuper.ui.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.inflate
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.bumptech.glide.RequestManager
import jahirfiquitiva.libs.archhelpers.ui.adapters.presenters.ListAdapterPresenter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.SectionedHeaderViewHolder
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.data.models.KuperKomponent
import jahirfiquitiva.libs.kuper.ui.adapters.viewholders.KuperViewHolder
import java.lang.ref.WeakReference

class KuperAdapter(
        private val context: WeakReference<Context>,
        private val manager: RequestManager,
        private val wallpaper: Drawable?,
        private val komponents: ArrayList<KuperKomponent>,
        private val listener: (Intent) -> Unit
                  ) :
        SectionedRecyclerViewAdapter<SectionedViewHolder>(), ListAdapterPresenter<KuperKomponent> {
    
    override fun get(index: Int): KuperKomponent = komponents[index]
    
    override fun clearList() {
        val size = itemCount
        komponents.clear()
        notifyItemRangeRemoved(0, size)
    }
    
    override fun addAll(newItems: ArrayList<KuperKomponent>) {
        val prevSize = itemCount
        komponents.addAll(newItems)
        notifyItemRangeInserted(prevSize, newItems.size)
    }
    
    override fun setItems(newItems: ArrayList<KuperKomponent>) {
        komponents.clear()
        komponents.addAll(newItems)
        notifyDataSetChanged()
    }
    
    override fun removeItem(item: KuperKomponent) {
        val prevSize = itemCount
        val index = komponents.indexOf(item)
        if (index < 0) return
        komponents.remove(item)
        notifyItemRangeRemoved(index, prevSize)
    }
    
    override fun updateItem(item: KuperKomponent) {
        val prevSize = itemCount
        val index = komponents.indexOf(item)
        if (index < 0) return
        notifyItemRangeChanged(index, prevSize)
    }
    
    override fun addItem(newItem: KuperKomponent) {
        val prevSize = itemCount
        komponents.add(newItem)
        notifyItemRangeInserted(prevSize, itemCount)
    }
    
    init {
        shouldShowHeadersForEmptySections(false)
        shouldShowFooters(false)
    }
    
    fun getHeadersBeforePosition(position: Int): Int {
        var headers = 0
        (0 until position)
                .filter { isHeader(it) }
                .forEach { headers += 1 }
        return headers
    }
    
    override fun getItemViewType(
            section: Int, relativePosition: Int,
            absolutePosition: Int
                                ): Int = section
    
    override fun onBindViewHolder(
            holder: SectionedViewHolder?, section: Int, relativePosition: Int,
            absolutePosition: Int
                                 ) {
        holder?.let {
            if (it is KuperViewHolder) {
                when (section) {
                    0 -> it.bind(
                            komponents.filter { it.type == KuperKomponent.Type.ZOOPER }[relativePosition],
                            manager, wallpaper)
                    1 -> it.bind(
                            komponents.filter { it.type == KuperKomponent.Type.KOMPONENT }[relativePosition],
                            manager, wallpaper)
                    2 -> it.bind(
                            komponents.filter { it.type == KuperKomponent.Type.WIDGET }[relativePosition],
                            manager, wallpaper, listener)
                    3 -> it.bind(
                            komponents.filter { it.type == KuperKomponent.Type.LOCKSCREEN }[relativePosition],
                            manager, wallpaper, listener)
                    4 -> it.bind(
                            komponents.filter { it.type == KuperKomponent.Type.WALLPAPER }[relativePosition],
                            manager, wallpaper, listener)
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SectionedViewHolder? {
        return if (viewType >= 0) {
            parent?.inflate(R.layout.item_komponent)?.let { KuperViewHolder(it) }
        } else parent?.inflate(R.layout.item_section_header)?.let { SectionedHeaderViewHolder(it) }
    }
    
    override fun getItemCount(section: Int): Int = when (section) {
        0 -> komponents.filter { it.type == KuperKomponent.Type.ZOOPER }.size
        1 -> komponents.filter { it.type == KuperKomponent.Type.KOMPONENT }.size
        2 -> komponents.filter { it.type == KuperKomponent.Type.WIDGET }.size
        3 -> komponents.filter { it.type == KuperKomponent.Type.LOCKSCREEN }.size
        4 -> komponents.filter { it.type == KuperKomponent.Type.WALLPAPER }.size
        else -> 0
    }
    
    override fun onBindHeaderViewHolder(
            holder: SectionedViewHolder?, section: Int,
            expanded: Boolean
                                       ) {
        context.get()?.let {
            if (holder is SectionedHeaderViewHolder) {
                when (section) {
                    0 -> {
                        holder.setTitle(
                                "${it.getString(R.string.zooper_widget)} ${it.getString(
                                        R.string.templates)}")
                        holder.icon.gone()
                    }
                    1 -> {
                        holder.setTitle(it.getString(R.string.komponents))
                        holder.icon.gone()
                    }
                    2 -> {
                        holder.setTitle(
                                "${it.getString(R.string.kwgt)} ${it.getString(
                                        R.string.templates)}")
                        holder.icon.gone()
                    }
                    3 -> {
                        holder.setTitle(
                                "${it.getString(R.string.klck)} ${it.getString(
                                        R.string.templates)}")
                        holder.icon.gone()
                    }
                    4 -> {
                        holder.setTitle(
                                "${it.getString(R.string.klwp)} ${it.getString(
                                        R.string.templates)}")
                        holder.icon.gone()
                    }
                }
            }
        }
    }
    
    override fun getSectionCount(): Int = 5
    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
}