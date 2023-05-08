package dev.jahir.kuper.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.views.inflate
import dev.jahir.frames.ui.viewholders.SectionHeaderViewHolder
import dev.jahir.kuper.R
import dev.jahir.kuper.data.models.Component
import dev.jahir.kuper.ui.viewholders.ComponentViewHolder

class ComponentsAdapter(private val onClick: (Component) -> Unit) :
    SectionedRecyclerViewAdapter<SectionedViewHolder>() {

    var components = listOf<Component>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val sectionTitles = ArrayList<String>()

    internal var wallpaper: Drawable? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    internal fun updateSectionTitles(ctxt: Context?) {
        ctxt ?: return
        if (sectionTitles.isNotEmpty()) return
        sectionTitles.clear()
        sectionTitles.add(
            ctxt.string(R.string.x_templates, ctxt.string(R.string.zooper_widget))
        )
        sectionTitles.add(ctxt.string(R.string.komponents))
        sectionTitles.add(ctxt.string(R.string.x_templates, ctxt.string(R.string.kwgt)))
        sectionTitles.add(ctxt.string(R.string.x_templates, ctxt.string(R.string.klck)))
        sectionTitles.add(ctxt.string(R.string.x_templates, ctxt.string(R.string.klwp)))
        notifyDataSetChanged()
    }

    init {
        shouldShowHeadersForEmptySections(false)
        shouldShowFooters(false)
    }

    override fun getItemViewType(
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    ): Int = section

    private fun getComponentTypeForSection(section: Int): Component.Type = when (section) {
        0 -> Component.Type.ZOOPER
        1 -> Component.Type.KOMPONENT
        2 -> Component.Type.WIDGET
        3 -> Component.Type.LOCKSCREEN
        4 -> Component.Type.WALLPAPER
        else -> Component.Type.UNKNOWN
    }

    override fun onBindViewHolder(
        holder: SectionedViewHolder?,
        section: Int,
        relativePosition: Int,
        absolutePosition: Int
    ) {

        (holder as? ComponentViewHolder)?.bind(
            components.filter { it.type == getComponentTypeForSection(section) }[relativePosition],
            wallpaper,
            if (section >= 2) onClick else null
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder {
        return if (viewType >= 0) {
            ComponentViewHolder(parent.inflate(R.layout.item_component))
        } else SectionHeaderViewHolder(parent.inflate(R.layout.item_section_header))
    }

    override fun getItemCount(section: Int): Int =
        components.filter { it.type == getComponentTypeForSection(section) }.size

    override fun onBindHeaderViewHolder(
        holder: SectionedViewHolder?,
        section: Int,
        expanded: Boolean
    ) {
        (holder as? SectionHeaderViewHolder)?.bind(
            sectionTitles.getOrNull(section).orEmpty(),
            "", section > 0
        )
    }

    override fun getSectionCount(): Int = 5
    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
}