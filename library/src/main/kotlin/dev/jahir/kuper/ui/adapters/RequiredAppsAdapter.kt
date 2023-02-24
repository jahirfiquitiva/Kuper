package dev.jahir.kuper.ui.adapters

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.extensions.views.inflate
import dev.jahir.kuper.R
import dev.jahir.kuper.data.models.RequiredApp
import dev.jahir.kuper.ui.viewholders.RequiredAppViewHolder

class RequiredAppsAdapter(private val onClick: (RequiredApp) -> Unit) :
    RecyclerView.Adapter<RequiredAppViewHolder>() {

    var apps: List<RequiredApp> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: RequiredAppViewHolder, position: Int) {
        (holder as? RequiredAppViewHolder)?.bind(apps[position], onClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequiredAppViewHolder =
        RequiredAppViewHolder(parent.inflate(R.layout.item_setup))

    override fun getItemCount(): Int = apps.size
}
