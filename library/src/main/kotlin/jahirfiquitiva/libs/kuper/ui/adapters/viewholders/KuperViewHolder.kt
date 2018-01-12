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

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ca.allanwang.kau.utils.isVisible
import ca.allanwang.kau.utils.visibleIf
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import jahirfiquitiva.libs.frames.helpers.extensions.releaseFromGlide
import jahirfiquitiva.libs.frames.ui.adapters.viewholders.GlideSectionedViewHolder
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.dividerColor
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.isInPortraitMode
import jahirfiquitiva.libs.kauextensions.extensions.primaryTextColor
import jahirfiquitiva.libs.kauextensions.extensions.secondaryTextColor
import jahirfiquitiva.libs.kuper.R
import jahirfiquitiva.libs.kuper.data.models.KuperKomponent
import java.io.File

class KuperViewHolder(itemView: View) : GlideSectionedViewHolder(itemView) {
    private val wall: ImageView by itemView.bind(R.id.wall)
    private val preview: ImageView by itemView.bind(R.id.preview)
    private val details: LinearLayout by itemView.bind(R.id.komponent_details)
    private val name: TextView by itemView.bind(R.id.komponent_name)
    private val app: TextView by itemView.bind(R.id.komponent_app)
    private val icon: ImageView by itemView.bind(R.id.launch_app)
    
    fun bind(
            komponent: KuperKomponent,
            manager: RequestManager,
            wallpaper: Drawable?,
            listener: (KuperKomponent) -> Unit = {}
            ) {
        with(itemView) {
            wall.setImageDrawable(wallpaper)
            details.setBackgroundColor(context.dividerColor)
            name.setTextColor(context.primaryTextColor)
            name.text = komponent.name.formatCorrectly().replace("_", " ")
            app.setTextColor(context.secondaryTextColor)
            app.text = komponent.type.toString()
            icon.visibleIf(komponent.hasIntent)
            if (icon.isVisible) {
                icon.setImageDrawable("ic_open_app".getDrawable(context))
                icon.setOnClickListener { listener(komponent) }
            }
            val rightPreview = if (context.isInPortraitMode) komponent.previewPath else komponent.rightLandPath
            manager.load(File(rightPreview))
                    .apply(RequestOptions().priority(Priority.HIGH))
                    .into(preview)
                    .clearOnDetach()
        }
    }
    
    override fun onRecycled() {
        preview.releaseFromGlide()
    }
}