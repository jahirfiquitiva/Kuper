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
package jahirfiquitiva.libs.kuper.ui.adapters.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import jahirfiquitiva.libs.kext.extensions.accentColor
import jahirfiquitiva.libs.kext.extensions.bind
import jahirfiquitiva.libs.kext.extensions.drawable
import jahirfiquitiva.libs.kext.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kext.extensions.primaryTextColor
import jahirfiquitiva.libs.kext.extensions.secondaryTextColor
import jahirfiquitiva.libs.kext.ui.widgets.MaterialIconView
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.ui.adapters.KuperApp

class SetupViewHolder(itemView: View) : SectionedViewHolder(itemView) {
    private val title: TextView? by itemView.bind(R.id.app_name)
    private val description: TextView? by itemView.bind(R.id.app_description)
    private val button: AppCompatButton? by itemView.bind(R.id.install_button)
    private val icon: MaterialIconView? by itemView.bind(R.id.icon)
    
    fun bind(app: KuperApp, listener: (KuperApp) -> Unit) =
        with(itemView) {
            title?.setTextColor(context.primaryTextColor)
            title?.text = app.name
            description?.setTextColor(context.secondaryTextColor)
            description?.text = app.desc
            icon?.setImageDrawable(context.drawable(app.icon))
            button?.setTextColor(context.getPrimaryTextColorFor(context.accentColor, 0.6F))
            button?.setOnClickListener { listener(app) }
        }
}