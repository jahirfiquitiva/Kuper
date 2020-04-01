package dev.jahir.kuper.data.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jahir.frames.extensions.createIfDidNotExist
import dev.jahir.frames.extensions.deleteEverything
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.lazyMutableLiveData
import dev.jahir.kuper.data.models.Component
import dev.jahir.kuper.extensions.copyFromTo
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipFile

class ComponentsViewModel : ViewModel() {

    private val componentsData: MutableLiveData<ArrayList<Component>> by lazyMutableLiveData()

    val components: ArrayList<Component>
        get() = ArrayList(componentsData.value.orEmpty())

    fun loadComponents(context: Context?) {
        context ?: return
        viewModelScope.launch {
            val components = internalLoadComponents(context)
            componentsData.postValue(components)
        }
    }

    fun observe(owner: LifecycleOwner, onUpdated: (ArrayList<Component>) -> Unit = {}) {
        componentsData.observe(owner, Observer { onUpdated(it) })
    }

    fun destroy(owner: LifecycleOwner) {
        componentsData.removeObservers(owner)
    }

    private suspend fun internalLoadComponents(context: Context): ArrayList<Component> {
        return if (components.isNotEmpty()) ArrayList(components)
        else withContext(IO) {
            val folders = arrayOf("templates", "komponents", "widgets", "lockscreens", "wallpapers")
            val components = ArrayList<Component>()
            val assets = context.assets
            val previewsFolder = File(context.externalCacheDir, "KuperPreviews")
            previewsFolder.deleteEverything()
            previewsFolder.createIfDidNotExist()
            folders.forEachIndexed { index, folder ->
                val files = assets.list(folder)
                files?.forEach {
                    val type = Component.typeForKey(index)
                    if (type != Component.Type.UNKNOWN) {
                        if (it.endsWith(Component.extensionForType(type)) || it.endsWith(".zip")) {
                            val previewFile = File(previewsFolder, it)
                            val widgetName = it.substring(0, it.lastIndexOf("."))
                            val path = "$folder/$it"
                            try {
                                getWidgetPreviewsPathFromZip(
                                    widgetName,
                                    path,
                                    assets.open(path),
                                    previewsFolder,
                                    previewFile,
                                    type
                                )?.let { preview ->
                                    components.add(preview)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
            components
        }
    }

    private fun getWidgetPreviewsPathFromZip(
        name: String,
        path: String,
        ins: InputStream,
        folder: File,
        file: File,
        type: Component.Type
    ): Component? {
        return try {
            var out: OutputStream? = null

            val thumbnails = arrayOf("", "")
            if (type == Component.Type.KOMPONENT) {
                thumbnails[0] = "komponent_thumb"
            } else {
                if (type != Component.Type.ZOOPER) {
                    thumbnails[0] = "preset_thumb_portrait"
                    thumbnails[1] = "preset_thumb_landscape"
                }
            }

            val preview =
                File(
                    folder,
                    name + if (type == Component.Type.ZOOPER) ".png" else "_port.png"
                )
            val previewLand =
                if (type == Component.Type.WIDGET || type == Component.Type.WALLPAPER ||
                    type == Component.Type.LOCKSCREEN) {
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
                            if (type == Component.Type.ZOOPER) {
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
                    e.printStackTrace()
                } finally {
                    out?.flush()
                    out?.close()
                }

                if (type == Component.Type.ZOOPER) {
                    out = null
                    try {
                        val preBitmap: Bitmap? = BitmapFactory.decodeFile(preview.absolutePath)
                        val bmp: Bitmap? =
                            Component.clearBitmap(
                                preBitmap, Color.parseColor("#555555")
                            )
                        out = FileOutputStream(preview)
                        bmp?.compress(Bitmap.CompressFormat.PNG, 30, out)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        out?.flush()
                        out?.close()
                    }
                }
            }

            val correctName = try {
                if (type != Component.Type.ZOOPER)
                    name.substring(0, name.lastIndexOf('.'))
                else name
            } catch (e: Exception) {
                name
            }

            return Component(
                type, correctName, path, preview.absolutePath ?: "",
                previewLand?.absolutePath ?: ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}