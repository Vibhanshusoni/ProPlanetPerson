package com.example.proplanetperson

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.proplanetperson.databinding.ActivityAddPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPostBinding
    private var imageUri: Uri? = null
    private lateinit var loadingDialog: Dialog

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

        setupLoadingDialog()

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
                uploadPost(caption)
            }
        }
    }

    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setContentView(R.layout.dialog_loading)
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

    private fun uploadPost(caption: String) {
        loadingDialog.show()

        val postId = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("posts/$postId.jpg")

        imageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val postMap = hashMapOf(
                            "postId" to postId,
                            "imageUrl" to downloadUrl.toString(),
                            "caption" to caption,
                            "publisherId" to userId,
                            "timestamp" to System.currentTimeMillis()
                        )

                        FirebaseFirestore.getInstance()
                            .collection("posts")
                            .document(postId)
                            .set(postMap)
                            .addOnSuccessListener {
                                loadingDialog.dismiss()
                                Toast.makeText(this, "Post uploaded!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                loadingDialog.dismiss()
                                Toast.makeText(this, "Failed to post", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
