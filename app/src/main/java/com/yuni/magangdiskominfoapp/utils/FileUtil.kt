package com.yuni.magangdiskominfoapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtil {

    @SuppressLint("Range")
    fun getFileName(context: Context, uri: Uri?): String {
        var result: String? = null
        if (uri != null) {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
            cursor?.close()
        }
        return result ?: "unknown"
    }

    fun getFile(context: Context, uri: Uri?): File? {
        if (uri == null) return null

        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, getFileName(context, uri))
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}