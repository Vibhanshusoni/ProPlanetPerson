package com.example.proplanetperson.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.database.Cursor
import android.util.Log

fun getRealPathFromURI(context: Context, uri: Uri): String? {
    var cursor: Cursor? = null
    try {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        cursor = context.contentResolver.query(uri, proj, null, null, null)
        val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        return cursor?.getString(column_index!!)
    } catch (e: Exception) {
        Log.e("FileUtil", "Error getting real path from URI: ${e.message}", e)
        return null
    } finally {
        cursor?.close()
    }
}