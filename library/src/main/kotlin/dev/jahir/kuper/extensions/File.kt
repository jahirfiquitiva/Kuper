package dev.jahir.kuper.extensions

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

fun InputStream.copyFilesTo(os: OutputStream) {
    val buffer = ByteArray(2048)
    var readInt = 0
    while ({ readInt = read(buffer);readInt }() != -1) os.write(buffer, 0, readInt)
    os.flush()
}

fun ZipFile.copyFromTo(from: ZipEntry, to: File?) {
    to ?: return
    var zipIn: InputStream? = null
    var zipOut: OutputStream? = null
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