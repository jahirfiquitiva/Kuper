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
package jahirfiquitiva.libs.kuper.ui.adapters

import android.content.Context
import android.view.ViewGroup
import ca.allanwang.kau.utils.inflate
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import jahirfiquitiva.libs.frames.helpers.extensions.jfilter
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.SectionedHeaderViewHolder
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.ui.adapters.viewholders.SetupViewHolder
import java.lang.ref.WeakReference

class SetupAdapter(
        private val context: WeakReference<Context>,
        private val listener: (KuperApp) -> Unit
                  ) :
        SectionedRecyclerViewAdapter<SectionedViewHolder>() {
    
    private val apps = ArrayList<KuperApp>()
    
    fun updateApps(apps: ArrayList<KuperApp>) {
        this.apps.clear()
        this.apps.addAll(apps)
        notifyDataSetChanged()
    }
    
    init {
        shouldShowHeadersForEmptySections(false)
        shouldShowFooters(false)
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
            if (it is SetupViewHolder) {
                when (section) {
                    0 -> it.bind(
                            apps.jfilter { it.packageName.hasContent() }[relativePosition],
                            listener)
                    1 -> it.bind(
                            apps.jfilter { !it.packageName.hasContent() }[relativePosition],
                            listener)
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder {
        return if (viewType >= 0) {
            SetupViewHolder(parent.inflate(R.layout.item_app_to_setup))
        } else SectionedHeaderViewHolder(parent.inflate(R.layout.item_section_header))
    }
    
    override fun getItemCount(section: Int): Int = when (section) {
        0 -> apps.jfilter { it.packageName.hasContent() }.size
        1 -> apps.jfilter { (!it.packageName.hasContent()) }.size
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
                        holder.setTitle(it.getString(R.string.required_apps), false, false)
                    }
                    1 -> {
                        holder.setTitle(it.getString(R.string.assets), true, false)
                    }
                }
            }
        }
    }
    
    override fun getSectionCount(): Int = 2
    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
}

data class KuperApp(
        val name: String, val desc: String, val icon: String,
        val packageName: String = ""
                   )
