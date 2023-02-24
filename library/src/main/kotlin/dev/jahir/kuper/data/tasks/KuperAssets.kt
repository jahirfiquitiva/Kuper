package dev.jahir.kuper.data.tasks

import android.content.Context
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object KuperAssets {

    private val filesToIgnore =
        arrayOf("material-design-iconic-font", "materialdrawerfont", "google-material-font")

    @Suppress("BlockingMethodInNonBlockingContext", "RemoveExplicitTypeArguments")
    internal suspend fun listAssets(
        context: Context?,
        path: String = "",
        filterIgnoredFiles: Boolean = true
    ): Array<out String> {
        context ?: return arrayOf()
        return withContext(IO) {
            try {
                val initialList = context.assets.list(path).orEmpty()
                if (filterIgnoredFiles) {
                    initialList.filter {
                        !filesToIgnore.any { ignore -> it.startsWith(ignore) }
                    }.toTypedArray()
                } else initialList
            } catch (e: Exception) {
                arrayOf<String>()
            }
        }
    }

    internal suspend fun hasAssets(
        context: Context?,
        path: String = "",
        filterIgnoredFiles: Boolean = true
    ): Boolean = withContext(IO) {
        val assets = listAssets(context, path, filterIgnoredFiles)
        return@withContext assets.isNotEmpty()
    }

}
