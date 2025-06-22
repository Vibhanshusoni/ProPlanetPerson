package com.example.proplanetperson

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.proplanetperson.databinding.ActivityAccountSettingsBinding
import com.example.proplanetperson.models.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class AccountSettings : AppCompatActivity() {

    private lateinit var binding: ActivityAccountSettingsBinding
    private var firebaseUser: FirebaseUser? = null
    private var checker: String = ""
    private var myUrl: String = ""
    private var imageUri: Uri? = null
    private lateinit var storageProfileRef: StorageReference

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val cropImageOptions = CropImageOptions().apply {
                aspectRatioX = 1
                aspectRatioY = 1
                guidelines = CropImageView.Guidelines.ON
                fixAspectRatio = true
            }

            val cropImageContractOptions = CropImageContractOptions(uri, cropImageOptions)
            cropImageLauncher.launch(cropImageContractOptions)
        } ?: Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
    }

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
            imageUri?.let { binding.accountSettingsImageProfile.setImageURI(it) }
        } else {
            Log.e("AccountSettings", "Image cropping failed: ${result.error?.message}", result.error)
            Toast.makeText(this, "Image cropping cancelled or failed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
            return
        }

        storageProfileRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")
        getUserInfo()

        binding.accountSettingsLogoutbtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }

        binding.closeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }

        binding.accountSettingsChangeProfile.setOnClickListener {
            checker = "clicked"
            pickImageLauncher.launch("image/*")
        }

        binding.saveEditedInfo.setOnClickListener {
            if (checker == "clicked") uploadProfileImageAndInfo() else updateUserInfoOnly()
        }
    }

    private fun uploadProfileImageAndInfo() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image to upload.", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.accountSettingsFullnameProfile.text.isNullOrBlank() ||
            binding.accountSettingsUsernameProfile.text.isNullOrBlank()
        ) {
            Toast.makeText(this, "Full Name and Username are required.", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this).apply {
            setTitle("Profile Settings")
            setMessage("Please wait! Updating profile and image...")
            setCancelable(false)
            show()
        }

        val currentUser = firebaseUser ?: run {
            progressDialog.dismiss()
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val imageFileRef = storageProfileRef.child("${currentUser.uid}.png")
        val uploadTask = imageFileRef.putFile(imageUri!!)

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                progressDialog.dismiss()
                task.exception?.let { throw it }
            }
            imageFileRef.downloadUrl
        }).addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                myUrl = task.result.toString()
                val userMap = mapOf(
                    "fullname" to binding.accountSettingsFullnameProfile.text.toString(),
                    "username" to binding.accountSettingsUsernameProfile.text.toString().lowercase(),
                    "bio" to binding.accountSettingsBioProfile.text.toString(),
                    "image" to myUrl
                )

                FirebaseDatabase.getInstance().reference
                    .child("Users")
                    .child(currentUser.uid)
                    .updateChildren(userMap)
                    .addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            Toast.makeText(this, "Account updated successfully!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to update user info.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Failed to get download URL.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUserInfoOnly() {
        if (binding.accountSettingsFullnameProfile.text.isNullOrBlank() ||
            binding.accountSettingsUsernameProfile.text.isNullOrBlank()
        ) {
            Toast.makeText(this, "Full Name and Username are required.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = firebaseUser ?: run {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this).apply {
            setTitle("Profile Settings")
            setMessage("Please wait! Updating profile...")
            setCancelable(false)
            show()
        }

        val userMap = mapOf(
            "fullname" to binding.accountSettingsFullnameProfile.text.toString(),
            "username" to binding.accountSettingsUsernameProfile.text.toString().lowercase(),
            "bio" to binding.accountSettingsBioProfile.text.toString()
        )

        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(currentUser.uid)
            .updateChildren(userMap)
            .addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account updated successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getUserInfo() {
        val currentUser = firebaseUser ?: return

        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AccountSettings, "Failed to load user information.", Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let { user ->
                        Picasso.get()
                            .load(user.image)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(binding.accountSettingsImageProfile)

                        binding.accountSettingsFullnameProfile.setText(user.fullname)
                        binding.accountSettingsUsernameProfile.setText(user.username)
                        binding.accountSettingsBioProfile.setText(user.bio)
                    } ?: Toast.makeText(this@AccountSettings, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
