package com.example.proplanetperson

import android.Manifest
import android.app.Activity
import android.content.Context // Added for getImageUri helper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.* // For UUID
import com.example.proplanetperson.utils.SessionManager // Import SessionManager

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

    // Firebase Storage
    private lateinit var storageRef: StorageReference // Add StorageReference

    // SessionManager to get user ID
    private lateinit var sessionManager: SessionManager // Declare SessionManager

    // Activity Result Launchers
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_upload)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize Firebase Storage
        storageRef = FirebaseStorage.getInstance().reference.child("uploads") // Root folder for uploads

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

        // Initialize Activity Result Launchers
        setupActivityResultLaunchers()

        btnCamera.setOnClickListener { checkCameraPermissionAndOpen() }
        btnGallery.setOnClickListener { openGallery() }
        btnUpload.setOnClickListener { uploadMedia() }
    }

    private fun setupActivityResultLaunchers() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    // Convert Bitmap to Uri for consistent handling
                    selectedUri = getImageUri(applicationContext, it)
                    mediaPreview.setImageBitmap(it)
                    mediaPreview.visibility = ImageView.VISIBLE
                    btnUpload.visibility = Button.VISIBLE
                } ?: Toast.makeText(this, "Failed to capture image.", Toast.LENGTH_SHORT).show()
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedUri = uri
                    mediaPreview.setImageURI(uri)
                    mediaPreview.visibility = ImageView.VISIBLE
                    btnUpload.visibility = Button.VISIBLE
                } ?: Toast.makeText(this, "Failed to select image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper function to get a Uri from a Bitmap (needed for camera intent that returns Bitmap)
    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        // This is a common way to insert a bitmap into MediaStore and get its URI
        // Note: For Android 10+ (API 29+), direct insertion into MediaStore might be restricted
        // and you might need to use MediaStore.Images.Media.getContentUri method with a PendingIntent
        // for more robust file handling in scoped storage environments.
        // For simpler use cases, this often works.
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Image_" + UUID.randomUUID().toString(), null)
        return if (path != null) Uri.parse(path) else null
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun uploadMedia() {
        selectedUri?.let { uri ->
            progressBar.visibility = ProgressBar.VISIBLE
            btnUpload.isEnabled = false

            val caption = captionEditText.text.toString()
            val uid = sessionManager.getUserId() // GET UID FROM SESSIONMANAGER

            if (uid.isNullOrEmpty()) { // Check if UID is valid
                Toast.makeText(this, "User not logged in or session expired.", Toast.LENGTH_SHORT).show()
                progressBar.visibility = ProgressBar.GONE
                btnUpload.isEnabled = true
                return
            }

            val fileRef = storageRef.child(System.currentTimeMillis().toString() + ".jpg") // Unique filename
            val uploadTask = fileRef.putFile(uri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                fileRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result.toString()

                    val postRef = FirebaseDatabase.getInstance().getReference("posts").push()
                    val postId = postRef.key ?: UUID.randomUUID().toString() // Use UUID if key is null

                    val postData = mapOf(
                        "imageUrl" to downloadUri, // Store the download URL
                        "type" to mediaType,
                        "caption" to caption,
                        "timestamp" to System.currentTimeMillis(),
                        "uid" to uid, // Use UID from SessionManager
                        "postId" to postId
                        // You might want to add likesCount, commentsCount etc. here with initial 0
                    )

                    postRef.setValue(postData).addOnSuccessListener {
                        FirebaseDatabase.getInstance().getReference("user_posts")
                            .child(uid).child(postId).setValue(true)

                        Toast.makeText(this, "Uploaded & Saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to save post data: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("MediaUploadActivity", "Failed to save post data", e)
                    }.addOnCompleteListener {
                        progressBar.visibility = ProgressBar.GONE
                        btnUpload.isEnabled = true
                    }
                } else {
                    Toast.makeText(this, "Image upload failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    Log.e("MediaUploadActivity", "Image upload failed", task.exception)
                    progressBar.visibility = ProgressBar.GONE
                    btnUpload.isEnabled = true
                }
            }
        } ?: Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
    }
}