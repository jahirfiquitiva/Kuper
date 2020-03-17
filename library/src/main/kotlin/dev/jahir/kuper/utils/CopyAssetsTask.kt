package dev.jahir.kuper.utils

import android.content.Context
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object CopyAssetsTask {
    @Suppress("DEPRECATION")
    private suspend fun internalCopyAssets(context: Context, folder: String): Boolean =
        withContext(IO) {
            try {
                val files = context.assets.list(folder)
                files?.forEach {
                    if (it.contains(".") && !filesToIgnore.contains(it)) {
                        var ins: InputStream? = null
                        var out: OutputStream? = null
                        try {
                            ins = context.assets.open("$folder/$it")
                            val outFile = File(
                                "${Environment.getExternalStorageDirectory()}/" +
                                        "ZooperWidget/" + getCorrectFolderName(folder), it
                            )
                            outFile.parentFile?.mkdirs()
                            out = FileOutputStream(outFile)
                            ins.copyTo(out, 2048)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            ins?.close()
                            out?.flush()
                            out?.close()
                        }
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    fun copyAssets(activity: FragmentActivity?, folder: String, onFinish: (Boolean) -> Unit = {}) {
        activity ?: return
        activity.lifecycleScope.launch {
            val success = internalCopyAssets(activity, folder)
            onFinish(success)
        }
    }

    val filesToIgnore = arrayOf(
        "material-design-iconic-font-v2.2.0.ttf",
        "materialdrawerfont.ttf",
        "materialdrawerfont-font-v5.0.0.ttf",
        "google-material-font-v2.2.0.1.original.ttf",
        "google-material-font-v3.0.1.0.original.ttf"
    )

    fun getCorrectFolderName(folder: String): String = when (folder) {
        "fonts" -> "Fonts"
        "iconsets" -> "IconSets"
        "bitmaps" -> "Bitmaps"
        else -> folder
    }
}