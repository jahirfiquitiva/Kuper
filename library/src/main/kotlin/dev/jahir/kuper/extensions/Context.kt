package dev.jahir.kuper.extensions

import android.content.Context
import android.content.pm.PackageManager

fun Context.isAppInstalled(packageName: String): Boolean = try {
    packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    true
} catch (e: Exception) {
    false
}