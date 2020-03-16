package dev.jahir.kuper.ui.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import dev.jahir.frames.extensions.lazyViewModel
import dev.jahir.frames.ui.activities.FramesActivity
import dev.jahir.kuper.R
import dev.jahir.kuper.data.viewmodels.ComponentsViewModel
import dev.jahir.kuper.ui.fragments.ComponentsFragment

abstract class KuperActivity : FramesActivity() {

    private val componentsFragment: ComponentsFragment by lazy { ComponentsFragment.create() }
    private val componentsViewModel: ComponentsViewModel by lazyViewModel()

    override val initialFragmentTag: String = ComponentsFragment.TAG
    override val initialItemId: Int = R.id.widgets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        componentsViewModel.observe(this) { componentsFragment.updateItems(it) }
        componentsViewModel.loadComponents(this)
        requestStoragePermission()
    }

    override fun getNextFragment(itemId: Int): Pair<Pair<String?, Fragment?>?, Boolean>? {
        return when (itemId) {
            R.id.widgets -> Pair(Pair(ComponentsFragment.TAG, componentsFragment), true)
            else -> super.getNextFragment(itemId)
        }
    }
}