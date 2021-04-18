package dev.jahir.kuper.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

fun Context.isAppInstalled(packageName: String): Boolean = try {
    packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    true
} catch (e: Exception) {
    false
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

val Context.hasReadStoragePermission: Boolean
    get() = try {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    } catch (e: Exception) {
        false
    }

val Context.userWallpaper: Drawable?
    @SuppressLint("MissingPermission")
    get() {
        val wm = try {
            WallpaperManager.getInstance(this)
        } catch (e: Exception) {
            null
        }
        return try {
            wm?.let { nwm ->
                return if (hasReadStoragePermission)
                    try {
                        nwm.peekFastDrawable()
                    } catch (e: Exception) {
                        nwm.peekDrawable() ?: nwm.builtInDrawable ?: null
                    }
                else {
                    try {
                        nwm.peekDrawable() ?: nwm.builtInDrawable
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
