package dev.jahir.kuper.data.models

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.ColorInt
import dev.jahir.kuper.data.KLCK_PACKAGE
import dev.jahir.kuper.data.KLCK_PICKER
import dev.jahir.kuper.data.KLWP_PACKAGE
import dev.jahir.kuper.data.KLWP_PICKER
import dev.jahir.kuper.data.KWGT_PACKAGE
import dev.jahir.kuper.data.KWGT_PICKER

data class Component(
    val type: Type,
    val name: String,
    val path: String,
    val previewPath: String,
    private val previewLandPath: String = ""
) {

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + previewPath.hashCode()
        result = 31 * result + previewLandPath.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        return other is Component && other.type == type && other.name.equals(name, true) &&
                other.previewPath.equals(previewPath, true)
    }

    val rightLandPath = if (type.hasIntent) previewLandPath else previewPath

    fun getIntent(context: Context): Intent? {
        if (type.hasIntent) {
            val component: ComponentName = when (type) {
                Type.WALLPAPER -> ComponentName(KLWP_PACKAGE, KLWP_PICKER)
                Type.WIDGET -> ComponentName(KWGT_PACKAGE, KWGT_PICKER)
                Type.LOCKSCREEN -> ComponentName(KLCK_PACKAGE, KLCK_PICKER)
                else -> null
            } ?: return null

            val intent = Intent().apply { setComponent(component) }
            try {
                intent.data = Uri.Builder()
                    .scheme("kfile")
                    .authority("${context.packageName}.kustom.provider")
                    .appendPath(path)
                    .build()
            } catch (e: Exception) {
                intent.data = Uri.parse("kfile://${context.packageName}/$path")
            }
            return intent
        } else return null
    }

    enum class Type {
        KOMPONENT, WALLPAPER, WIDGET, LOCKSCREEN, UNKNOWN;

        val hasIntent: Boolean
            get() = this != UNKNOWN;
    }

    companion object {
        fun typeForKey(key: Int): Type = when (key) {
            0 -> Type.KOMPONENT
            1 -> Type.WIDGET
            2 -> Type.LOCKSCREEN
            3 -> Type.WALLPAPER
            else -> Type.UNKNOWN
        }

        fun extensionForType(type: Type) = when (type) {
            Type.KOMPONENT -> ".komp"
            Type.LOCKSCREEN -> ".klck"
            Type.WALLPAPER -> ".klwp"
            Type.WIDGET -> ".kwgt"
            Type.UNKNOWN -> ".zip"
        }

        fun extensionForKey(key: Int) = extensionForType(typeForKey(key))

        fun clearBitmap(bitmap: Bitmap?, @ColorInt colorToReplace: Int): Bitmap? {
            bitmap ?: return null

            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            var minX = width
            var minY = height
            var maxX = -1
            var maxY = -1

            val newBitmap = Bitmap.createBitmap(width, height, bitmap.config)
            var pixel: Int

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val index = y * width + x
                    pixel = pixels[index]
                    if (pixel == colorToReplace) {
                        pixels[index] = android.graphics.Color.TRANSPARENT
                    }
                    if (pixels[index] != android.graphics.Color.TRANSPARENT) {
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
            }

            newBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return Bitmap.createBitmap(newBitmap, minX, minY, maxX - minX + 1, maxY - minY + 1)
        }
    }
}
