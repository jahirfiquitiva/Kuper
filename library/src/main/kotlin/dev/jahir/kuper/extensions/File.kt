package dev.jahir.kuper.extensions

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private fun InputStream.copyFilesTo(os: OutputStream) {
    try {
        val buffer = ByteArray(2048)
        var readInt = 0
        while ({ readInt = read(buffer);readInt }() != -1) os.write(buffer, 0, readInt)
    } catch (e: Exception) {
    } finally {
        os.flush()
        os.close()
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
    } catch (e: Exception) {
    } finally {
        zipIn?.close()
        zipOut?.flush()
        zipOut?.close()
    }
}