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
package jahirfiquitiva.libs.kuper.providers.zooper

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

open class ZooperTemplateProvider : ContentProvider() {
    
    override fun openAssetFile(paramUri: Uri, paramString: String): AssetFileDescriptor? {
        if (paramUri.pathSegments.size > 0)
            try {
                if (context == null) return null
                val name = paramUri.path?.substring(1)
                name ?: return null
                return context?.assets?.openFd(name)
            } catch (e: Exception) {
                return null
            }
        return null
    }
    
    override fun query(
        paramUri: Uri,
        paramArrayOfString1: Array<out String>?,
        paramString1: String?,
        paramArrayOfString2: Array<out String>?,
        paramString2: String?
                      ): Cursor {
        val cursor = MatrixCursor(arrayOf("string"))
        try {
            if (context == null) return cursor
            val path = paramUri.path?.substring(1)
            path?.let {
                val items = context?.assets?.list(it)
                items?.let {
                    for (s in it) {
                        cursor.newRow().add(s)
                        cursor.moveToNext()
                    }
                }
            }
            cursor.moveToFirst()
        } catch (e: Exception) {
            cursor.close()
            throw RuntimeException(e)
        }
        return cursor
    }
    
    override fun getType(paramUri: Uri): String? = null
    override fun onCreate(): Boolean = false
    override fun insert(paramUri: Uri, paramContentValues: ContentValues?): Uri? = null
    override fun update(
        paramUri: Uri, paramContentValues: ContentValues?,
        paramString: String?,
        paramArrayOfString: Array<out String>?
                       ): Int = 0
    
    override fun delete(
        paramUri: Uri,
        paramString: String?,
        paramArrayOfString: Array<out String>?
                       ): Int = 0
}