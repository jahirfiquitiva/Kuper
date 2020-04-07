package dev.jahir.kuper.ui.adapters

import android.view.ViewGroup
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.views.inflate
import dev.jahir.frames.ui.viewholders.SectionHeaderViewHolder
import dev.jahir.kuper.R
import dev.jahir.kuper.data.models.RequiredApp
import dev.jahir.kuper.ui.viewholders.RequiredAppViewHolder

class RequiredAppsAdapter(private val onClick: (RequiredApp) -> Unit) :
    SectionedRecyclerViewAdapter<SectionedViewHolder>() {

    var apps: ArrayList<RequiredApp> = ArrayList()
        set(value) {
            field.clear()
            field.addAll(value)
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

    private fun itemForSection(section: Int, relativePosition: Int): RequiredApp =
        apps.filter {
            if (section == 0) it.packageName.hasContent()
            else !it.packageName.hasContent()
        }[relativePosition]

    override fun onBindViewHolder(
        holder: SectionedViewHolder?, section: Int, relativePosition: Int,
        absolutePosition: Int
    ) {
        (holder as? RequiredAppViewHolder)?.bind(itemForSection(section, relativePosition), onClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionedViewHolder {
        return if (viewType >= 0) {
            RequiredAppViewHolder(parent.inflate(R.layout.item_setup))
        } else SectionHeaderViewHolder(parent.inflate(R.layout.item_section_header))
    }

    override fun getItemCount(section: Int): Int =
        apps.filter {
            if (section == 0) it.packageName.hasContent()
            else !it.packageName.hasContent()
        }.size

    override fun onBindHeaderViewHolder(
        holder: SectionedViewHolder?,
        section: Int,
        expanded: Boolean
    ) {
        (holder as? SectionHeaderViewHolder)?.bind(
            if (section == 0) R.string.required_apps else R.string.assets,
            0, section > 0
        )
    }

    override fun getSectionCount(): Int = 2
    override fun onBindFooterViewHolder(holder: SectionedViewHolder?, section: Int) {}
}