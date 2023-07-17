package dev.jahir.kuper.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.extensions.context.color
import dev.jahir.frames.extensions.context.drawable
import dev.jahir.frames.extensions.context.resolveColor
import dev.jahir.frames.extensions.resources.tint
import dev.jahir.frames.extensions.views.context
import dev.jahir.frames.extensions.views.findView
import dev.jahir.kuper.R
import dev.jahir.kuper.data.models.RequiredApp

class RequiredAppViewHolder(itemView: View) : SectionedViewHolder(itemView) {
    private val icon: AppCompatImageView? by itemView.findView(R.id.stat_icon)
    private val title: TextView? by itemView.findView(R.id.stat_title)
    private val description: TextView? by itemView.findView(R.id.stat_description)
    private val button: AppCompatButton? by itemView.findView(R.id.required_app_button)

    fun bind(app: RequiredApp, listener: (RequiredApp) -> Unit) {
        icon?.setImageDrawable(
            context.drawable(app.icon)
                ?.tint(
                    context.resolveColor(
                        com.google.android.material.R.attr.colorOnSurface,
                        context.color(dev.jahir.frames.R.color.onSurface)
                    )
                )
        )
        title?.text = app.name
        description?.text = app.description
        button?.setOnClickListener { listener(app) }
    }
}
