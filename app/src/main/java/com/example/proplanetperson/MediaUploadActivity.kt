package com.example.proplanetperson

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class MediaUploadActivity : AppCompatActivity() {

    private lateinit var mediaType: String
    private lateinit var headerText: TextView
    private lateinit var captionEditText: EditText
    private lateinit var btnCamera: Button
    private lateinit var btnGallery: Button
    private lateinit var btnUpload: Button
    private lateinit var mediaPreview: ImageView
    private lateinit var progressBar: ProgressBar

    private var selectedUri: Uri? = null
    private var capturedBitmap: Bitmap? = null

    private val REQUEST_CAMERA = 1001
    private val REQUEST_GALLERY = 1002
    private val CAMERA_PERMISSION_CODE = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_upload)

        mediaType = intent.getStringExtra("mediaType") ?: "photo"

        headerText = findViewById(R.id.media_type)
        captionEditText = findViewById(R.id.captionEditText)
        btnCamera = findViewById(R.id.btn_camera)
        btnGallery = findViewById(R.id.btn_gallery)
        btnUpload = findViewById(R.id.btnUpload)
        mediaPreview = findViewById(R.id.media_preview)
        progressBar = findViewById(R.id.progressBar)

        headerText.text = "Upload $mediaType"
        progressBar.visibility = ProgressBar.GONE

        btnCamera.setOnClickListener { checkCameraPermissionAndOpen() }
        btnGallery.setOnClickListener { openGallery() }
        btnUpload.setOnClickListener { uploadMedia() }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        capturedBitmap = it
                        selectedUri = null
                        mediaPreview.setImageBitmap(it)
                        mediaPreview.visibility = ImageView.VISIBLE
                        btnUpload.visibility = Button.VISIBLE
                    }
                }

                REQUEST_GALLERY -> {
                    val uri = data?.data
                    uri?.let {
                        selectedUri = it
                        capturedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        mediaPreview.setImageBitmap(capturedBitmap)
                        mediaPreview.visibility = ImageView.VISIBLE
                        btnUpload.visibility = Button.VISIBLE
                    }
                }
            }
        }
    }

    private fun uploadMedia() {
        if (capturedBitmap == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE
        btnUpload.isEnabled = false

        // Convert image to Base64
        val baos = ByteArrayOutputStream()
        capturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val imageBytes = baos.toByteArray()
        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val caption = captionEditText.text.toString()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val postRef = FirebaseDatabase.getInstance().getReference("posts").push()
        val postId = postRef.key ?: return

        val postData = mapOf(
            "imageBase64" to base64Image,
            "type" to mediaType,
            "caption" to caption,
            "timestamp" to System.currentTimeMillis(),
            "uid" to uid,
            "postId" to postId
        )

        postRef.setValue(postData).addOnSuccessListener {
            FirebaseDatabase.getInstance().getReference("user_posts")
                .child(uid).child(postId).setValue(true)

            Toast.makeText(this, "Uploaded & Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }.addOnCompleteListener {
            progressBar.visibility = ProgressBar.GONE
            btnUpload.isEnabled = true
        }
    }
}
