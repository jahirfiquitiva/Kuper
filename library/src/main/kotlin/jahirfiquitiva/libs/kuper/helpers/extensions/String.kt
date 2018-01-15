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
package jahirfiquitiva.libs.kuper.helpers.extensions

import android.content.Context
import jahirfiquitiva.libs.kuper.helpers.utils.CopyAssetsTask

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
                    if (!(CopyAssetsTask.filesToIgnore.contains(it))) list.add(it)
                }
            }
        }
        return list
    } catch (e: Exception) {
        ArrayList()
    }
}