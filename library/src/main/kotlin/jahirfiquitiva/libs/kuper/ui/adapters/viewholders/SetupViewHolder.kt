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
package jahirfiquitiva.libs.kuper.ui.adapters.viewholders

import android.support.v7.widget.AppCompatButton
import android.view.View
import android.widget.TextView
import ca.allanwang.kau.utils.bindView
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import jahirfiquitiva.libs.frames.ui.widgets.MaterialIcon
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.primaryTextColor
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.ui.adapters.KuperApp

class SetupViewHolder(itemView:View):SectionedViewHolder(itemView) {
    private val title:TextView by bindView(R.id.app_name)
    private val description:TextView by bindView(R.id.app_description)
    private val button:AppCompatButton by bindView(R.id.install_button)
    private val icon:MaterialIcon by bindView(R.id.icon)
    
    fun bind(app:KuperApp, listener:(KuperApp) -> Unit) =
            with(itemView) {
                title.setTextColor(context.primaryTextColor)
                title.text = app.name
                description.setTextColor(context.secondaryTextColor)
                description.text = app.desc
                icon.setImageDrawable(app.icon.getDrawable(context))
                button.setBackgroundColor(context.accentColor)
                button.setOnClickListener { listener(app) }
            }
}