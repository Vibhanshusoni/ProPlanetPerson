package com.example.proplanetperson

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.canhub.cropper.*
import com.example.proplanetperson.api.ApiClient
import com.example.proplanetperson.api.PostRepositoryImpl
import com.example.proplanetperson.api.UserRepositoryImpl // Needed for PostViewModel factory
import com.example.proplanetperson.ui.post.PostViewModel
import com.example.proplanetperson.utils.Resource
import com.example.proplanetperson.utils.SessionManager
import com.example.proplanetperson.utils.getRealPathFromURI
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private lateinit var sessionManager: SessionManager
    private lateinit var postViewModel: PostViewModel
    private lateinit var loadingDialog: Dialog

    private var currentUserId: String? = null
    private var authToken: String? = null

    private val cropImageLauncher =
        registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val uriContent = result.uriContent
                if (uriContent != null) {
                    imageUri = uriContent
                    // Now that we have the image, initiate the upload process
                    uploadStory()
                } else {
                    Toast.makeText(this, "Image URI is null after cropping.", Toast.LENGTH_SHORT).show()
                    finish() // Close if image is not ready
                }
            } else {
                val error = result.error
                Toast.makeText(this, "Cropping failed: ${error?.message}", Toast.LENGTH_LONG).show()
                Log.e("AddStoryActivity", "Cropping failed", error)
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story) // Assuming you still use this layout

        sessionManager = SessionManager(this)

        currentUserId = sessionManager.getUserId()
        authToken = sessionManager.getAuthToken()

        if (currentUserId.isNullOrEmpty() || authToken.isNullOrEmpty()) {
            Toast.makeText(this, "You need to be logged in to add a story.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize PostViewModel
        val postRepository = PostRepositoryImpl(ApiClient.postApi)
        val userRepository = UserRepositoryImpl(ApiClient.userApi)
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return PostViewModel(postRepository, userRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        postViewModel = ViewModelProvider(this, viewModelFactory).get(PostViewModel::class.java)

        setupLoadingDialog()

        // Observe the createStoryResult LiveData
        postViewModel.createStoryResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    loadingDialog.show()
                }
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Story Added!!", Toast.LENGTH_SHORT).show()
                    finish()
                    postViewModel.resetCreateStoryResult()
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    val errorMessage = resource.message ?: "Failed to add story."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("AddStoryActivity", "Story upload error: $errorMessage")
                    finish()
                    postViewModel.resetCreateStoryResult()
                }
                is Resource.Idle -> { // <--- ADD THIS BRANCH
                    // Do nothing or reset specific UI elements
                    Log.d("AddStoryActivity", "Story creation state is idle.")
                }
            }
        }

        // Launch image cropper immediately when activity is created, as per original logic
        val cropImageOptions = CropImageContractOptions(
            uri = null, // Will launch image picker
            cropImageOptions = CropImageOptions().apply {
                aspectRatioX = 9
                aspectRatioY = 16
                guidelines = CropImageView.Guidelines.ON
            }
        )
        cropImageLauncher.launch(cropImageOptions)
    }

    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setContentView(R.layout.dialog_loading) // Re-using dialog_loading.xml
    }

    private fun uploadStory() {
        if (imageUri == null) {
            Toast.makeText(this, "No image selected for story.", Toast.LENGTH_SHORT).show()
            finish() // Close if no image
            return
        }

        val userId = currentUserId
        val token = authToken

        if (userId.isNullOrEmpty() || token.isNullOrEmpty()) {
            Toast.makeText(this, "User not authenticated to upload story.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            // Get a real file path from the content URI
            val filePath = getRealPathFromURI(this, imageUri!!)
            if (filePath == null) {
                Toast.makeText(this, "Failed to get image file.", Toast.LENGTH_SHORT).show()
                Log.e("AddStoryActivity", "Could not get real path for URI: $imageUri")
                finish()
                return
            }

            val file = File(filePath)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val timeEnd = System.currentTimeMillis() + 86400000 // 24 hours
            val userIdPart = userId.toRequestBody("text/plain".toMediaTypeOrNull())
            val timeEndPart = timeEnd.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            postViewModel.createStory(token, imagePart, userIdPart, timeEndPart)

        } catch (e: Exception) {
            Log.e("AddStoryActivity", "Error preparing story image for upload: ${e.message}", e)
            Toast.makeText(this, "Failed to prepare image for story upload.", Toast.LENGTH_SHORT).show()
            finish() // Close on error
        }
    }
}