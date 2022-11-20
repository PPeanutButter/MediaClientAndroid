package com.peanut.ted.ed.utils

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.provider.OpenableColumns
import androidx.core.net.toFile
import com.peanut.ted.ed.utils.Unities.toast
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


object FileCompat {
    fun copyFile(source: Uri, destination: String, context: Context) {
        source.let { returnUri ->
            when (returnUri.scheme) {
                "file" -> {
                    val fis = FileInputStream(returnUri.toFile())
                    val fos = FileOutputStream(destination)
                    copyFileUseStream(fis, fos)
                    fis.close()
                    fos.apply { this.flush() }.close()
                    return
                }
                "content" -> {
                    val sharedDB = context.contentResolver.openFileDescriptor(source, "r")
                    sharedDB?.let {
                        val fd = it.fileDescriptor
                        val fis = FileInputStream(fd)
                        val fos = FileOutputStream(destination)
                        copyFileUseStream(fis, fos)
                        fis.close()
                        fos.apply { this.flush() }.close()
                        it.close()
                        return
                    }
                }
                else -> "不支持的协议:${returnUri.scheme}".toast(context)
            }
        }
        "copyFile error:fileDescriptor is NULL($source)".toast(context)
    }

    fun copyFile(
        source: Uri,
        destination: String,
        context: Context,
        func: (path: String) -> Unit
    ) {
        object : Thread() {
            override fun run() {
                copyFile(source, destination, context)
                Handler(context.mainLooper).post {
                    func(destination)
                }
            }
        }.start()
    }

    private fun copyFileUseStream(
        fileInputStream: InputStream,
        fileOutputStream: OutputStream,
        close: Boolean = true
    ) {
        try {
            val buffer = ByteArray(1024)
            var byteRead: Int
            while (-1 != fileInputStream.read(buffer).also { byteRead = it }) {
                fileOutputStream.write(buffer, 0, byteRead)
            }
            fileOutputStream.flush()
            if (close) {
                fileInputStream.close()
                fileOutputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFileNameAndSize(
        context: Context,
        uri: Uri?,
        withoutExtension: Boolean = false
    ): Pair<String, Long> {
        try {
            uri?.let { returnUri ->
                when (returnUri.scheme) {
                    "file" -> {
                        val file = returnUri.toFile()
                        return (if (withoutExtension) file.nameWithoutExtension else file.name) to file.length()
                    }
                    "content" -> {
                        context.contentResolver.query(returnUri, null, null, null, null)?.use {
                            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                            return if (it.moveToFirst())
                                (if (withoutExtension) it.getString(nameIndex).substring(
                                    0,
                                    it.getString(nameIndex).lastIndexOf(".")
                                ) else it.getString(nameIndex)) to it.getLong(sizeIndex)
                            else "无法查询到文件名与大小(系统未索引该文件)" to 0L
                        }
                    }
                    else -> return "不支持的协议:${returnUri.scheme}" to 0L
                }
            }
        } catch (e: Exception) {
            return "错误:${e.localizedMessage}" to 0L
        }
        return "错误:获取文件信息失败" to 0L
    }
}
