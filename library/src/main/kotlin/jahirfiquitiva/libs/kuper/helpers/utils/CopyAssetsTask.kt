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
package jahirfiquitiva.libs.kuper.helpers.utils

import android.content.Context
import android.os.Environment
import jahirfiquitiva.libs.archhelpers.tasks.QAsync
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference

class CopyAssetsTask(
        param: WeakReference<Context>,
        folder: String,
        doOnSuccess: (Boolean) -> Unit
                    ) :
        QAsync<Context, Boolean>(
                param,
                object : QAsync.Callback<Context, Boolean>() {
                    override fun doLoad(param: Context): Boolean? {
                        return try {
                            val files = param.assets.list(folder)
                            files?.forEach {
                                if (it.contains(".") && !filesToIgnore.contains(it)) {
                                    var ins: InputStream? = null
                                    var out: OutputStream? = null
                                    try {
                                        ins = param.assets.open("$folder/$it")
                                        val outFile = File(
                                                "${Environment.getExternalStorageDirectory()}/" +
                                                        "ZooperWidget/" +
                                                        getCorrectFolderName(folder), it)
                                        outFile.parentFile.mkdirs()
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
                            KuperLog.e { e.message }
                            false
                        }
                    }
                    
                    override fun onSuccess(result: Boolean) {
                        doOnSuccess(result)
                    }
                }) {
    
    companion object {
        val filesToIgnore = arrayOf(
                "material-design-iconic-font-v2.2.0.ttf",
                "materialdrawerfont.ttf",
                "materialdrawerfont-font-v5.0.0.ttf",
                "google-material-font-v2.2.0.1.original.ttf",
                "google-material-font-v3.0.1.0.original.ttf")
        
        fun getCorrectFolderName(folder: String): String = when (folder) {
            "fonts" -> "Fonts"
            "iconsets" -> "IconSets"
            "bitmaps" -> "Bitmaps"
            else -> folder
        }
    }
}