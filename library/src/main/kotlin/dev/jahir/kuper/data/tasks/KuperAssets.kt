package dev.jahir.kuper.data.tasks

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

object KuperAssets {

    private val filesToIgnore =
        arrayOf("material-design-iconic-font", "materialdrawerfont", "google-material-font")

    private val zooperFolders = arrayOf("fonts", "iconsets", "bitmaps")

    internal fun listAssets(
        context: Context?,
        path: String = "",
        filterIgnoredFiles: Boolean = true
    ): Array<out String> {
        context ?: return arrayOf()
        return try {
            val initialList = context.assets.list(path).orEmpty()
            if (filterIgnoredFiles) {
                initialList.filter {
                    !filesToIgnore.any { ignore -> it.startsWith(ignore) }
                }.toTypedArray()
            } else initialList
        } catch (e: Exception) {
            arrayOf()
        }
    }

    internal fun hasAssets(
        context: Context?,
        path: String = "",
        filterIgnoredFiles: Boolean = true
    ): Boolean = listAssets(context, path, filterIgnoredFiles).size >= 0

    private fun getZooperAssets(context: Context?): List<Pair<String, String>> {
        val assets = ArrayList<Pair<String, String>>()
        zooperFolders.forEach { folder ->
            assets.addAll(listAssets(context, folder).map { Pair(folder, it) })
        }
        return assets.filter { it.second.contains(".") }
    }

    private fun zooperAssetPath(asset: Pair<String, String>): String =
        "${asset.first}/${asset.second}"

    private fun getCorrectFolderName(folder: String): String = when (folder) {
        "fonts" -> "Fonts"
        "iconsets" -> "IconSets"
        "bitmaps" -> "Bitmaps"
        else -> folder
    }

    @Suppress("DEPRECATION")
    private fun zooperAssetPathOnDevice(asset: Pair<String, String>): String =
        "${Environment.getExternalStorageDirectory()}/ZooperWidget/${getCorrectFolderName(asset.first)}/${asset.second}"

    internal fun areZooperAssetsInstalled(context: Context?): Boolean {
        var installedAssetsCount = 0
        val expectedAssets = getZooperAssets(context)
        expectedAssets.forEach {
            val file = File(zooperAssetPathOnDevice(it))
            if (file.exists()) installedAssetsCount += 1
        }
        return installedAssetsCount >= expectedAssets.size
    }

    @Suppress("DEPRECATION")
    private suspend fun internalCopyAssets(context: Context?): Boolean {
        context ?: return false
        return withContext(IO) {
            try {
                getZooperAssets(context).forEach { file ->
                    var ins: InputStream? = null
                    var out: OutputStream? = null
                    try {
                        ins = context.assets.open(zooperAssetPath(file))
                        val outFile = File(zooperAssetPathOnDevice(file))
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
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun copyZooperAssets(activity: FragmentActivity?, onFinish: (Boolean) -> Unit = {}) {
        activity ?: return
        activity.lifecycleScope.launch {
            val success = internalCopyAssets(activity)
            onFinish(success)
        }
    }
}