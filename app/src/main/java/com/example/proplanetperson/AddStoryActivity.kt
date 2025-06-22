package com.example.proplanetperson

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.*
import com.example.proplanetperson.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddStoryActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageStoryRef: StorageReference? = null

    private val cropImageLauncher =
        registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val uriContent = result.uriContent
                if (uriContent != null) {
                    imageUri = uriContent
                    uploadStory()
                }
            } else {
                Toast.makeText(this, "Cropping failed: ${result.error}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        storageStoryRef = FirebaseStorage.getInstance().reference.child("Story Pictures")

        val cropImageOptions = CropImageContractOptions(
            uri = null,
            cropImageOptions = CropImageOptions().apply {
                aspectRatioX = 9
                aspectRatioY = 16
                guidelines = CropImageView.Guidelines.ON
            }
        )
        cropImageLauncher.launch(cropImageOptions)
    }

    private fun uploadStory() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select Image", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Adding Story")
        progressDialog.setMessage("Please wait while your story is added")
        progressDialog.show()

        val fileRef = storageStoryRef!!.child(System.currentTimeMillis().toString() + ".jpg")
        val uploadTask = fileRef.putFile(imageUri!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
                progressDialog.dismiss()
            }
            return@continueWithTask fileRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result
                myUrl = downloadUrl.toString()

                val ref = FirebaseDatabase.getInstance().reference
                    .child("Story")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)

                val storyId = ref.push().key.toString()
                val timeEnd = System.currentTimeMillis() + 86400000 // 24 hours

                val storyMap = hashMapOf(
                    "userid" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "timestart" to ServerValue.TIMESTAMP,
                    "timeend" to timeEnd,
                    "imageurl" to myUrl,
                    "storyid" to storyId
                )

                ref.child(storyId).updateChildren(storyMap)
                Toast.makeText(this, "Story Added!!", Toast.LENGTH_SHORT).show()
                finish()
            }
            progressDialog.dismiss()
        }
    }
}
