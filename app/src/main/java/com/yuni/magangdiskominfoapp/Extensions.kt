package com.yuni.magangdiskominfoapp

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun Uri.toFile(context: Context): File? {
    return try {
        val stream = context.contentResolver.openInputStream(this)
        if (stream != null) {
            val file = File.createTempFile("temp_", null, context.cacheDir)
            FileOutputStream(file).use { output ->
                stream.copyTo(output)
            }
            stream.close()
            file
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}