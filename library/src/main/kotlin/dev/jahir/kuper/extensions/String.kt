package dev.jahir.kuper.extensions

import android.content.Context
import dev.jahir.kuper.data.tasks.CopyAssetsTask

internal fun String.inAssetsAndWithContent(context: Context): Boolean {
    val folders = context.assets.list("").orEmpty()
    return try {
        if (folders.contains(this)) {
            return getFilesInAssetsFolder(context).isNotEmpty()
        } else false
    } catch (e: Exception) {
        false
    }
}

internal fun String.getFilesInAssetsFolder(context: Context): ArrayList<String> =
    try {
        ArrayList(
            context.assets.list(this).orEmpty()
                .filter { !CopyAssetsTask.filesToIgnore.contains(it) }
        )
    } catch (e: Exception) {
        arrayListOf()
    }