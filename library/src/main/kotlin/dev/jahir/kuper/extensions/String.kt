package dev.jahir.kuper.extensions

import android.content.Context
import dev.jahir.kuper.utils.CopyAssetsTask

internal fun String.inAssetsAndWithContent(context: Context): Boolean {
    val folders = context.assets.list("")
    return try {
        if (folders != null) {
            if (folders.contains(this)) {
                return getFilesInAssetsFolder(context).isNotEmpty()
            } else false
        } else false
    } catch (e: Exception) {
        false
    }
}

internal fun String.getFilesInAssetsFolder(context: Context): ArrayList<String> {
    val list = ArrayList<String>()
    return try {
        val files = context.assets.list(this)
        if (files != null) {
            if (files.isNotEmpty()) {
                files.forEach {
                    if (!CopyAssetsTask.filesToIgnore.contains(it)) list.add(it)
                }
            }
        }
        return list
    } catch (e: Exception) {
        ArrayList()
    }
}