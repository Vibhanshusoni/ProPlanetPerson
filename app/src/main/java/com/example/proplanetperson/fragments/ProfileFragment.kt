package com.example.proplanetperson.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log // Import Log for debugging
import android.view.LayoutInflater // Keep this, as it's needed for inflate
import android.view.View
import android.view.ViewGroup // Keep this, as it's needed for inflate
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proplanetperson.R
import com.example.proplanetperson.adapters.UserPostAdapter
import com.example.proplanetperson.models.Post // Ensure this import is correct for your Post data class
import com.example.proplanetperson.utils.SessionManager // NEW: Import SessionManager
// Removed Firebase Auth imports
// import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    // UI elements
    private lateinit var profileImage: ImageView
    private lateinit var username: TextView
    private lateinit var userBio: TextView
    private lateinit var editProfileBtn: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: UserPostAdapter
    private val postList = mutableListOf<Post>() // Initialize immediately

    // Firebase related
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var postsRef: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var profilePicRef: StorageReference

    // Session Management
    private lateinit var sessionManager: SessionManager // NEW: Declare SessionManager

    private val PICK_IMAGE_REQUEST = 71

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SessionManager
        sessionManager = SessionManager(requireContext()) // Use requireContext() as fragment context is available

        // Get current user ID using SessionManager
        val currentUserId = sessionManager.getUserId()
        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "You must be logged in to view your profile.", Toast.LENGTH_LONG).show()
            // Optionally navigate back or to login screen
            return
        }

        // Initialize UI components
        profileImage = view.findViewById(R.id.profile_image_profile)
        username = view.findViewById(R.id.username_in_profile)
        userBio = view.findViewById(R.id.bio_profile)
        editProfileBtn = view.findViewById(R.id.edit_profile_Button)
        recyclerView = view.findViewById(R.id.recyclerview_profile)

        setupRecyclerView()

        // Initialize Firebase instances with the currentUserId
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users").child(currentUserId) // "users" is already lowercase
        postsRef = database.getReference("posts") // IMPORTANT: Changed "Posts" to "posts" for consistency
        storage = FirebaseStorage.getInstance()
        profilePicRef = storage.reference.child("profile_pictures").child("$currentUserId.jpg")

        // Load data
        loadUserInfo(currentUserId)
        loadUserPosts(currentUserId)

        // Set listeners
        editProfileBtn.setOnClickListener {
            openImagePicker()
        }
    }

    private fun setupRecyclerView() {
        postAdapter = UserPostAdapter(requireContext(), postList)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = postAdapter
    }

    // Pass currentUserId explicitly
    private fun loadUserInfo(currentUserId: String) {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if data exists for the user
                if (snapshot.exists()) {
                    val usernameStr = snapshot.child("username").getValue(String::class.java) ?: "User"
                    val bioStr = snapshot.child("bio").getValue(String::class.java) ?: "No bio available"
                    val profileUrl = snapshot.child("image").getValue(String::class.java)

                    username.text = usernameStr
                    userBio.text = bioStr

                    Glide.with(requireContext())
                        .load(profileUrl)
                        .placeholder(R.drawable.ic_profile_placeholder) // Ensure this drawable exists
                        .error(R.drawable.ic_profile_placeholder) // Fallback for loading errors
                        .into(profileImage)
                } else {
                    Log.w("ProfileFragment", "User data not found for ID: $currentUserId")
                    username.text = "Profile Not Found"
                    userBio.text = "No data available."
                    Glide.with(requireContext())
                        .load(R.drawable.ic_profile_placeholder)
                        .into(profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Failed to load user info: ${error.message}", error.toException())
                Toast.makeText(requireContext(), "Error loading profile info.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Pass currentUserId explicitly
    private fun loadUserPosts(currentUserId: String) {
        postsRef.orderByChild("publisher").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear() // Clear existing posts
                    for (postSnap in snapshot.children) {
                        val post = postSnap.getValue(Post::class.java)
                        post?.let { postList.add(it) } // Safely add post if not null
                    }
                    postAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Failed to load user posts: ${error.message}", error.toException())
                    Toast.makeText(requireContext(), "Error loading user posts.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            val imageUri: Uri = data?.data ?: run {
                Toast.makeText(requireContext(), "No image selected.", Toast.LENGTH_SHORT).show()
                return
            }
            uploadProfileImage(imageUri)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(requireContext(), "Image selection cancelled.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Failed to pick image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val currentUserId = sessionManager.getUserId()
        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please log in to upload image.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show() // Indicate upload start
        val uploadTask = profilePicRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                updateProfileImageUrl(uri.toString())
            }
        }.addOnFailureListener { exception ->
            Log.e("ProfileFragment", "Image upload failed: ${exception.message}", exception)
            Toast.makeText(requireContext(), "Image upload failed: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateProfileImageUrl(imageUrl: String) {
        val currentUserId = sessionManager.getUserId()
        if (currentUserId.isNullOrEmpty()) {
            // This case should ideally not happen if uploadProfileImage was called after a check
            Toast.makeText(requireContext(), "Login session expired. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        userRef.child("image").setValue(imageUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile image updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ProfileFragment", "Failed to update profile image URL: ${task.exception?.message}", task.exception)
                    Toast.makeText(requireContext(), "Failed to update profile image: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}