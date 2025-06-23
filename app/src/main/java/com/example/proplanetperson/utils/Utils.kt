package com.example.proplanetperson.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class Utils {
    fun shareMedia(context: Context, mediaUrl: String, mimeType: String) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mediaUrl)

        // Create a temporary file to download the media
        val localFile = File.createTempFile("shared_", mimeType.substringAfter("/"))
        val filePath = File(context.cacheDir, localFile.name) // Use app's cache directory

        storageRef.getFile(filePath).addOnSuccessListener {
            // Get Uri for the file using FileProvider
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", filePath)

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = mimeType
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission to the receiving app
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to prepare file for sharing", Toast.LENGTH_SHORT).show()
        }
    }
}