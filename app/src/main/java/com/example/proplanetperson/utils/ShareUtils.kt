package com.example.proplanetperson.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import java.io.File

fun shareMedia(context: Context, mediaUrl: String, mimeType: String) {
    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mediaUrl)

    val localFile = File.createTempFile("shared_", mimeType.substringAfter("/"))
    val filePath = File(context.cacheDir, localFile.name)

    storageRef.getFile(filePath).addOnSuccessListener {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", filePath)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }.addOnFailureListener {
        Toast.makeText(context, "Failed to download media for sharing", Toast.LENGTH_SHORT).show()
    }
}
