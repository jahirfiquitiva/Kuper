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
package jahirfiquitiva.libs.kuper.providers.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import jahirfiquitiva.libs.archhelpers.viewmodels.ListViewModel
import jahirfiquitiva.libs.frames.helpers.extensions.maxPictureRes
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kuper.data.models.KuperKomponent
import jahirfiquitiva.libs.kuper.helpers.extensions.clean
import jahirfiquitiva.libs.kuper.helpers.extensions.copyFromTo
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class KuperViewModel : ListViewModel<Context, KuperKomponent>() {
    override fun internalLoad(param: Context): ArrayList<KuperKomponent> {
        val folders = arrayOf("templates", "komponents", "widgets", "lockscreens", "wallpapers")
        val komponents = ArrayList<KuperKomponent>()
        val assets = param.assets
        val previewsFolder = File(param.externalCacheDir, "KuperPreviews")
        previewsFolder.clean()
        previewsFolder.mkdirs()
        folders.forEachIndexed { index, folder ->
            val files = assets.list(folder)
            files?.forEach {
                val type = KuperKomponent.typeForKey(index)
                if (type != KuperKomponent.Type.UNKNOWN) {
                    val previewFile = File(previewsFolder, it)
                    val widgetName = it.substring(0, it.lastIndexOf("."))
                    komponents.add(
                            getWidgetPreviewsPathFromZip(
                                    param, widgetName,
                                    assets.open("$folder/$it"),
                                    previewsFolder, previewFile, type))
                }
            }
        }
        return komponents
    }
    
    private fun getWidgetPreviewsPathFromZip(
            context: Context,
            name: String,
            ins: InputStream,
            folder: File,
            file: File,
            type: KuperKomponent.Type
                                            ): KuperKomponent {
        var out: OutputStream? = null
        
        val thumbnails = arrayOf("", "")
        if (type == KuperKomponent.Type.KOMPONENT) {
            thumbnails[0] = "komponent_thumb"
        } else {
            if (type != KuperKomponent.Type.ZOOPER) {
                thumbnails[0] = "preset_thumb_portrait"
                thumbnails[1] = "preset_thumb_landscape"
            }
        }
        
        val preview = File(
                folder,
                name + (if (type == KuperKomponent.Type.ZOOPER) ".png" else "_port.jpg"))
        val previewLand = if (type == KuperKomponent.Type.WIDGET ||
                type == KuperKomponent.Type.WALLPAPER ||
                type == KuperKomponent.Type.LOCKSCREEN) {
            File(folder, "${name}_land.jpg")
        } else null
        
        try {
            out = FileOutputStream(file)
            ins.copyTo(out, 2048)
            ins.close()
            out.flush()
            out.close()
            
            if (file.exists()) {
                val zipFile = ZipFile(file)
                val entries = zipFile.entries()
                var entry: ZipEntry? = entries.nextElement()
                while (entry != null) {
                    if (type == KuperKomponent.Type.ZOOPER) {
                        val endsWith = entry.name?.endsWith("screen.png") ?: false
                        if (endsWith) {
                            zipFile.copyFromTo(entry, preview)
                            break
                        }
                    } else {
                        if (!entry.name.contains("/") && entry.name.contains("thumb")) {
                            if (entry.name.contains(thumbnails[0])) {
                                zipFile.copyFromTo(entry, preview)
                            } else if (thumbnails[1].hasContent() &&
                                    entry.name.contains(thumbnails[1])) {
                                zipFile.copyFromTo(entry, previewLand)
                            }
                        }
                    }
                    
                    entry = try {
                        if (entries.hasMoreElements()) entries.nextElement()
                        else null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            out?.flush()
            out?.close()
        }
        
        if (type == KuperKomponent.Type.ZOOPER) {
            out = null
            try {
                val bmp = KuperKomponent.clearBitmap(
                        BitmapFactory.decodeFile(preview.absolutePath),
                        Color.parseColor("#555555"))
                out = FileOutputStream(preview)
                bmp.compress(Bitmap.CompressFormat.PNG, context.maxPictureRes * 2, out)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                out?.flush()
                out?.close()
            }
        }
        
        val correctName = if (type != KuperKomponent.Type.ZOOPER)
            name.substring(0, name.lastIndexOf(".")) else name
        return KuperKomponent(
                type, correctName, preview.absolutePath, previewLand?.absolutePath ?: "")
    }
}