package dev.jahir.kuper.ui.viewholders

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.api.load
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.extensions.context
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.extensions.lower
import dev.jahir.frames.extensions.visibleIf
import dev.jahir.frames.utils.tint
import dev.jahir.kuper.R
import dev.jahir.kuper.data.models.Component
import dev.jahir.kuper.extensions.isInPortraitMode
import java.io.File

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
        app?.text = component.type.toString().lower().capitalize()
        icon?.visibleIf(component.hasIntent)
        if (icon?.isVisible == true) {
            icon?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_open_app))
            icon?.setOnClickListener { listener?.invoke(component) }
        }
        val rightPreview =
            if (context.isInPortraitMode) component.previewPath else component.rightLandPath
        try {
            progress?.indeterminateDrawable?.tint(Color.parseColor("#888"))
        } catch (e: Exception) {
        }
        preview?.load(File(rightPreview)) {
            listener { _, _ -> progress?.gone() }
        }
    }
}