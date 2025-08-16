package dev.jahir.kuper.extensions

import androidx.annotation.IdRes
import dev.jahir.frames.ui.widgets.FramesBottomNavigationView

fun FramesBottomNavigationView.removeItem(@IdRes itemId: Int) {
    try {
        menu.removeItem(itemId)
    } catch (e: Exception) {
    }
}
