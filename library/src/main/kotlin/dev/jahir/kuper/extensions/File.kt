package dev.jahir.kuper.extensions

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private fun InputStream.copyFilesTo(os: OutputStream) {
    try {
        val buffer = ByteArray(4096)
        var bytes = 0
        while (read(buffer).also { bytes = it } != -1)
            os.write(buffer, 0, bytes)
    } catch (_: Exception) {
    } finally {
        try {
            os.flush()
            os.close()
        } catch (_: Exception) {
        }
    }
}

fun ZipFile.copyFromTo(from: ZipEntry, to: File?) {
    to ?: return
    var zipIn: InputStream? = null
    var zipOut: OutputStream? = null
    try {
        zipOut = FileOutputStream(to)
        zipIn = getInputStream(from)
        zipIn.copyFilesTo(zipOut)
    } catch (_: Exception) {
    } finally {
        try {
            zipIn?.close()
            zipOut?.flush()
            zipOut?.close()
        } catch (_: Exception) {
        }
    }
}
