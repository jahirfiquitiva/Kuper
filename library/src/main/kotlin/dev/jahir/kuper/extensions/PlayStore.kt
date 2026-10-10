package dev.jahir.kuper.extensions

import android.content.Context
import android.net.Uri
import dev.jahir.frames.ui.activities.base.BaseLicenseCheckerActivity.Companion.PLAY_STORE_LINK_PREFIX

/**
 * Builds the Play Store link for [packageName] including an install referrer
 * that identifies this app (its own package name) as the source of the install.
 *
 * The target app can later read it through the Play Install Referrer API as
 * `utm_source=<this app's package>&utm_medium=kuper&utm_campaign=<campaign>`.
 */
fun Context.playStoreLinkWithReferrer(packageName: String, campaign: String): String {
    val source = this.packageName
    val referrer = Uri.encode("utm_source=$source&utm_medium=kuper&utm_campaign=$campaign")
    return "$PLAY_STORE_LINK_PREFIX$packageName&referrer=$referrer"
}
