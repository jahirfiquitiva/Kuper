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
import jahirfiquitiva.libs.kext.extensions.deleteEverything
import jahirfiquitiva.libs.kext.extensions.hasContent
import jahirfiquitiva.libs.kuper.data.models.KuperKomponent
import jahirfiquitiva.libs.kuper.helpers.extensions.clean
import jahirfiquitiva.libs.kuper.helpers.extensions.copyFromTo
import jahirfiquitiva.libs.kuper.helpers.utils.KL
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
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
                    if (it.endsWith(KuperKomponent.extensionForType(type)) || it.endsWith(".zip")) {
                        val previewFile = File(previewsFolder, it)
                        val widgetName = it.substring(0, it.lastIndexOf("."))
                        val path = "$folder/$it"
                        try {
                            getWidgetPreviewsPathFromZip(
                                param, widgetName, path, assets.open(path), previewsFolder,
                                previewFile, type)?.let {
                                komponents.add(it)
                            }
                        } catch (e: Exception) {
                            KL.e(e.message)
                        }
                    }
                }
            }
        }
        return komponents
    }
    
    private fun getWidgetPreviewsPathFromZip(
        context: Context,
        name: String,
        path: String,
        ins: InputStream,
        folder: File,
        file: File,
        type: KuperKomponent.Type
                                            ): KuperKomponent? {
        return try {
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
            
            val preview =
                File(
                    folder,
                    name + if (type == KuperKomponent.Type.ZOOPER) ".png" else "_port.png")
            val previewLand =
                if (type == KuperKomponent.Type.WIDGET || type == KuperKomponent.Type.WALLPAPER ||
                    type == KuperKomponent.Type.LOCKSCREEN) {
                    File(folder, "${name}_land.png")
                } else null
            
            if (!preview.exists()) {
                try {
                    out = FileOutputStream(file)
                    ins.copyTo(out, 2048)
                    ins.close()
                    out.flush()
                    out.close()
                    
                    if (file.exists()) {
                        val zipFile = ZipFile(file)
                        val entries = zipFile.entries()
                        
                        while (entries.hasMoreElements()) {
                            val entry = entries.nextElement()
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
                        }
                    }
                } catch (e: Exception) {
                    KL.e(e.message)
                } finally {
                    out?.flush()
                    out?.close()
                }
                
                if (type == KuperKomponent.Type.ZOOPER) {
                    out = null
                    try {
                        preview.absolutePath?.let {
                            val preBitmap: Bitmap? = BitmapFactory.decodeFile(it)
                            val bmp: Bitmap? =
                                KuperKomponent.clearBitmap(
                                    preBitmap, Color.parseColor("#555555"))
                            out = FileOutputStream(preview)
                            bmp?.compress(Bitmap.CompressFormat.PNG, context.maxPictureRes, out)
                        }
                    } catch (e: Exception) {
                        KL.e(e.message)
                    } finally {
                        out?.flush()
                        out?.close()
                    }
                }
            }
            
            val correctName = try {
                if (type != KuperKomponent.Type.ZOOPER)
                    name.substring(0, name.lastIndexOf('.'))
                else name
            } catch (e: Exception) {
                name
            }
            
            return KuperKomponent(
                type, correctName, path, preview.absolutePath ?: "",
                previewLand?.absolutePath ?: "")
        } catch (e: Exception) {
            KL.e(e.message)
            null
        }
    }
}