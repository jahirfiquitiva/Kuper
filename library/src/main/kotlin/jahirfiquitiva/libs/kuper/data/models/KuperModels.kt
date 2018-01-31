/*
 * Copyright (c) 2018. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jahirfiquitiva.libs.kuper.data.models

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.annotation.ColorInt
import jahirfiquitiva.libs.kuper.helpers.utils.KLCK_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KLCK_PICKER
import jahirfiquitiva.libs.kuper.helpers.utils.KLWP_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KLWP_PICKER
import jahirfiquitiva.libs.kuper.helpers.utils.KWGT_PACKAGE
import jahirfiquitiva.libs.kuper.helpers.utils.KWGT_PICKER

data class KuperKomponent(
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
        return other is KuperKomponent && other.type == type && other.name.equals(name, true) &&
                other.previewPath.equals(previewPath, true)
    }
    
    val hasIntent = type == Type.WIDGET || type == Type.LOCKSCREEN || type == Type.WALLPAPER
    val rightLandPath = if (hasIntent) previewLandPath else previewPath
    
    fun getIntent(context: Context): Intent? {
        if (hasIntent) {
            val component: ComponentName = when (type) {
                KuperKomponent.Type.WALLPAPER -> ComponentName(KLWP_PACKAGE, KLWP_PICKER)
                KuperKomponent.Type.WIDGET -> ComponentName(KWGT_PACKAGE, KWGT_PICKER)
                KuperKomponent.Type.LOCKSCREEN -> ComponentName(KLCK_PACKAGE, KLCK_PICKER)
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
        ZOOPER, KOMPONENT, WALLPAPER, WIDGET, LOCKSCREEN, UNKNOWN
    }
    
    companion object {
        fun typeForKey(key: Int): Type = when (key) {
            0 -> Type.ZOOPER
            1 -> Type.KOMPONENT
            2 -> Type.WIDGET
            3 -> Type.LOCKSCREEN
            4 -> Type.WALLPAPER
            else -> Type.UNKNOWN
        }
        
        fun extensionForType(type: Type) = when (type) {
            Type.ZOOPER -> ".zw"
            Type.KOMPONENT -> ".komp"
            Type.LOCKSCREEN -> ".klck"
            Type.WALLPAPER -> ".klwp"
            Type.WIDGET -> ".kwgt"
            Type.UNKNOWN -> ".zip"
        }
        
        fun extensionForKey(key: Int) = extensionForType(typeForKey(key))
        
        fun clearBitmap(bitmap: Bitmap, @ColorInt colorToReplace: Int): Bitmap {
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
            return Bitmap.createBitmap(newBitmap, minX, minY, (maxX - minX) + 1, (maxY - minY) + 1)
        }
    }
}