package dev.jahir.kuper.ui.viewholders

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import coil.load
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.extensions.context.boolean
import dev.jahir.frames.extensions.context.drawable
import dev.jahir.frames.extensions.resources.lower
import dev.jahir.frames.extensions.resources.tint
import dev.jahir.frames.extensions.views.context
import dev.jahir.frames.extensions.views.findView
import dev.jahir.frames.extensions.views.gone
import dev.jahir.frames.extensions.views.visibleIf
import dev.jahir.kuper.R
import dev.jahir.kuper.data.models.Component
import java.io.File
import java.util.*

class ComponentViewHolder(itemView: View) : SectionedViewHolder(itemView) {
    private val wall: AppCompatImageView? by itemView.findView(R.id.device_wallpaper)
    private val preview: AppCompatImageView? by itemView.findView(R.id.component_preview)
    private val name: TextView? by itemView.findView(R.id.component_name)
    private val app: TextView? by itemView.findView(R.id.component_app)
    private val icon: AppCompatImageView? by itemView.findView(R.id.launch_app_button)
    private val progress: ProgressBar? by itemView.findView(R.id.component_progress)

    @SuppressLint("DefaultLocale")
    fun bind(
        component: Component,
        wallpaper: Drawable?,
        listener: ((Component) -> Unit)? = {}
    ) {
        wall?.setImageDrawable(wallpaper)
        name?.text = component.name.replace("_", " ")
        app?.text = component.type.toString().lower()
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                else it.toString()
            }
        icon?.visibleIf(component.hasIntent)
        if (icon?.isVisible == true) {
            icon?.setImageDrawable(
                context.drawable(R.drawable.ic_open_app)?.tint(Color.parseColor("#ffffff"))
            )
            icon?.setOnClickListener { listener?.invoke(component) }
        }
        val rightPreview =
            if (context.boolean(R.bool.is_landscape)) component.rightLandPath
            else component.previewPath
        try {
            progress?.indeterminateDrawable?.tint(Color.parseColor("#888888"))
        } catch (e: Exception) {
        }
        preview?.load(File(rightPreview)) {
            listener { _, _ -> progress?.gone() }
        }
    }
}
