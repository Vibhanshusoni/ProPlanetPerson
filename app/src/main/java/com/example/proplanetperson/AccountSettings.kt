package com.example.proplanetperson

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.proplanetperson.databinding.ActivityAccountSettingsBinding
import com.example.proplanetperson.models.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.example.proplanetperson.utils.SessionManager

class AccountSettings : AppCompatActivity() {

    private lateinit var binding: ActivityAccountSettingsBinding
    private lateinit var sessionManager: SessionManager

    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    // Activity Result Launcher for image cropping
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
            binding.accountSettingsImageProfile.setImageURI(imageUri) // CHANGED HERE
        } else {
            val exception = result.error
            Toast.makeText(this, "Image cropping failed: ${exception?.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val currentUserId = sessionManager.getUserId()
        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        binding.accountSettingsLogoutbtn.setOnClickListener { // CHANGED HERE
            sessionManager.logout()
            val intent = Intent(this@AccountSettings, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.accountSettingsChangeProfile.setOnClickListener {
            checker = "clicked"
            val options = CropImageOptions().apply {
                aspectRatioX = 1
                aspectRatioY = 1
                cropShape = CropImageView.CropShape.OVAL
                fixAspectRatio = true
            }
            cropImage.launch(
                CropImageContractOptions(
                    uri = null, // No initial URI, will pick from gallery
                    cropImageOptions = options // Pass the configured options object
                )
            )
        }

        binding.saveEditedInfo.setOnClickListener {
            if (checker == "clicked") {
                uploadImageAndUpdateInfo(currentUserId)
            } else {
                updateUserInfoOnly(currentUserId)
            }
        }

        userInfo(currentUserId)
    }

    private fun uploadImageAndUpdateInfo(currentUserId: String) {
        when {
            imageUri == null -> Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show()
            binding.accountSettingsFullnameProfile.text.toString().isEmpty() -> Toast.makeText(this, "Please write full name.", Toast.LENGTH_SHORT).show()
            binding.accountSettingsUsernameProfile.text.toString().isEmpty() -> Toast.makeText(this, "Please write username.", Toast.LENGTH_SHORT).show()
            binding.accountSettingsBioProfile.text.toString().isEmpty() -> Toast.makeText(this, "Please write your bio.", Toast.LENGTH_SHORT).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Updating profile...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child("$currentUserId.jpg")

                val uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        myUrl = task.result.toString()

                        val userRef = FirebaseDatabase.getInstance().reference
                            .child("Users")
                            .child(currentUserId)

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = binding.accountSettingsFullnameProfile.text.toString()
                        userMap["username"] = binding.accountSettingsUsernameProfile.text.toString()
                        userMap["bio"] = binding.accountSettingsBioProfile.text.toString()
                        userMap["image"] = myUrl

                        userRef.updateChildren(userMap).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to update profile: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                            progressDialog.dismiss()
                        }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Image upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateUserInfoOnly(currentUserId: String) {
        when {
            binding.accountSettingsFullnameProfile.text.toString().isEmpty() -> Toast.makeText(this, "Please write full name.", Toast.LENGTH_SHORT).show()
            binding.accountSettingsUsernameProfile.text.toString().isEmpty() -> Toast.makeText(this, "Please write username.", Toast.LENGTH_SHORT).show()
            binding.accountSettingsBioProfile.text.toString().isEmpty() -> Toast.makeText(this, "Please write your bio.", Toast.LENGTH_SHORT).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Updating profile...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val userRef = FirebaseDatabase.getInstance().reference
                    .child("Users")
                    .child(currentUserId)

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = binding.accountSettingsFullnameProfile.text.toString()
                userMap["username"] = binding.accountSettingsUsernameProfile.text.toString()
                userMap["bio"] = binding.accountSettingsBioProfile.text.toString()

                userRef.updateChildren(userMap).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to update profile: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun userInfo(currentUserId: String) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(currentUserId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        binding.accountSettingsFullnameProfile.setText(it.fullname)
                        binding.accountSettingsUsernameProfile.setText(it.username)
                        binding.accountSettingsBioProfile.setText(it.bio)
                        // CHANGED HERE
                        Picasso.get().load(it.image).placeholder(R.drawable.profile).into(binding.accountSettingsImageProfile)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AccountSettings", "Failed to load user info: ${error.message}", error.toException())
            }
        })
    }
}