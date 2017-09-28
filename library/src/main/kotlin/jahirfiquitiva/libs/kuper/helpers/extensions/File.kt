/*
 * Copyright (c) 2017. Jahir Fiquitiva
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

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

fun File.clean():Int {
    if (!(exists())) return 0
    var count = 0
    if (isDirectory) {
        listFiles().forEach { count += it.clean() }
    }
    delete()
    return count
}

fun InputStream.copyFilesTo(os:OutputStream) {
    val buffer = ByteArray(2048)
    var readInt = 0
    while ({ readInt = read(buffer);readInt }() != -1) os.write(buffer, 0, readInt)
    os.flush()
}

fun ZipFile.copyFromTo(from:ZipEntry, to:File?) {
    to ?: return
    var zipIn:InputStream? = null
    var zipOut:OutputStream? = null
    try {
        zipIn = getInputStream(from)
        zipOut = FileOutputStream(to)
        zipIn.copyTo(zipOut, 2048)
    } finally {
        zipIn?.close()
        zipOut?.flush()
        zipOut?.close()
    }
}