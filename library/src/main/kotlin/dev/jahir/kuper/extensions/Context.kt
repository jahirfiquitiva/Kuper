package dev.jahir.kuper.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.core.content.ContextCompat

fun Context.isAppInstalled(packageName: String): Boolean = try {
    packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    true
} catch (e: Exception) {
    false
}

val Context.isInHorizontalMode: Boolean
    get() = currentRotation == 90 || currentRotation == 270

val Context.isInPortraitMode: Boolean
    get() = currentRotation == 0 || currentRotation == 180

val Context.currentRotation: Int
    get() {
        val display = (getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
        return (display?.rotation ?: 0) * 90
    }

val Context.hasStoragePermission: Boolean
    get() = try {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    } catch (e: Exception) {
        false
    }