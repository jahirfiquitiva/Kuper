package jahirfiquitiva.libs.kuper.helpers.extensions

import android.content.Context
import android.content.pm.PackageManager

/**
 * Utils originally created by Allan Wang
 * Available at https://github.com/AllanWang/KAU
 * I have added them here (copy/pasted) because this lib doesn't really uses/needs all its features
 * at a 100%.
 * Anyway, full credits go to Allan, for these awesome extensions
 */

fun Context.isAppInstalled(packageName: String): Boolean = try {
    packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    true
} catch (e: Exception) {
    false
}