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
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.proplanetperson.databinding.ActivityAddPostBinding
import com.example.proplanetperson.api.ApiClient
import com.example.proplanetperson.api.PostRepositoryImpl
import com.example.proplanetperson.api.UserRepositoryImpl // Needed for PostViewModel factory
import com.example.proplanetperson.ui.post.PostViewModel // Import your PostViewModel
import com.example.proplanetperson.utils.Resource
import com.example.proplanetperson.utils.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File // Used to create a file from URI
import java.util.* // Used for UUID


class AddPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPostBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var postViewModel: PostViewModel // Declare PostViewModel
    private lateinit var loadingDialog: Dialog

    private var imageUri: Uri? = null
    private var currentUserId: String? = null
    private var authToken: String? = null

    // ✅ Crop Image Result Launcher
    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
            Glide.with(this).load(imageUri).into(binding.pictureToBePosted)
        } else {
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Get user ID and token from SessionManager
        currentUserId = sessionManager.getUserId()
        authToken = sessionManager.getAuthToken()

        if (currentUserId.isNullOrEmpty() || authToken.isNullOrEmpty()) {
            Toast.makeText(this, "You need to be logged in to post.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize PostViewModel
        val postRepository = PostRepositoryImpl(ApiClient.postApi)
        val userRepository = UserRepositoryImpl(ApiClient.userApi) // Pass UserRepository to PostViewModel
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

        // Observe the createPostResult LiveData
        postViewModel.createPostResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    loadingDialog.show()
                }
                is Resource.Success -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Post uploaded successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                    postViewModel.resetCreatePostResult()
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    val errorMessage = resource.message ?: "Failed to upload post."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("AddPostActivity", "Post upload error: $errorMessage")
                    postViewModel.resetCreatePostResult()
                }
                is Resource.Idle -> { // <--- ADD THIS BRANCH
                    // Do nothing or reset specific UI elements
                    Log.d("AddPostActivity", "Post creation state is idle.")
                }
            }
        }


        binding.pictureToBePosted.setOnClickListener {
            launchImageCropper()
        }

        binding.dontPostPicture.setOnClickListener {
            finish()
        }

        binding.postPicture.setOnClickListener {
            val caption = binding.writePost.text.toString().trim()
            if (imageUri == null || caption.isEmpty()) {
                Toast.makeText(this, "Image and caption required", Toast.LENGTH_SHORT).show()
            } else {
                currentUserId?.let { userId ->
                    authToken?.let { token ->
                        // Call ViewModel to upload post
                        uploadPost(caption, userId, token)
                    }
                } ?: Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setContentView(R.layout.dialog_loading)
        // Ensure you have dialog_loading.xml in your res/layout folder
        // It could be a simple ProgressBar or Lottie animation
    }

    private fun launchImageCropper() {
        val cropOptions = CropImageContractOptions(
            uri = null,
            cropImageOptions = CropImageOptions().apply {
                guidelines = CropImageView.Guidelines.ON
                aspectRatioX = 1
                aspectRatioY = 1
                cropShape = CropImageView.CropShape.RECTANGLE
            }
        )
        cropImageLauncher.launch(cropOptions)
    }

    private fun uploadPost(caption: String, userId: String, authToken: String) {
        imageUri?.let { uri ->
            try {
                val file = File(uri.path!!) // Get file path from URI
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                val captionPart = caption.toRequestBody("text/plain".toMediaTypeOrNull())
                val publisherIdPart = userId.toRequestBody("text/plain".toMediaTypeOrNull())

                postViewModel.createPost(authToken, imagePart, captionPart, publisherIdPart)

            } catch (e: Exception) {
                Log.e("AddPostActivity", "Error converting image URI to file: ${e.message}", e)
                Toast.makeText(this, "Failed to prepare image for upload.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}