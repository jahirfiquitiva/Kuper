package dev.jahir.kuper.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import dev.jahir.frames.extensions.context.getPackageInfoCompat

fun Context.isAppInstalled(packageName: String): Boolean = try {
    val info = packageManager.getPackageInfoCompat(packageName, PackageManager.GET_ACTIVITIES)
    info.packageName == packageName
} catch (_: Exception) {
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
                        nwm.peekFastDrawable() ?: nwm.fastDrawable
                    } catch (e: Exception) {
                        try {
                            nwm.peekDrawable() ?: nwm.builtInDrawable
                        } catch (e: Exception) {
                            nwm.builtInDrawable
                        }
                    }
                else {
                    try {
                        nwm.peekDrawable() ?: nwm.builtInDrawable
                    } catch (e: Exception) {
                        nwm.builtInDrawable
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
