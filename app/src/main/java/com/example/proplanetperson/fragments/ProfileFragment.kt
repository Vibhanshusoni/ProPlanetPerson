package com.example.proplanetperson.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proplanetperson.R
import com.example.proplanetperson.adapters.UserPostAdapter
import com.example.proplanetperson.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.widget.Toast

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var profileImage: ImageView
    private lateinit var username: TextView
    private lateinit var userBio: TextView
    private lateinit var editProfileBtn: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: UserPostAdapter
    private val postList = mutableListOf<Post>()

    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var postsRef: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var profilePicRef: StorageReference

    private val PICK_IMAGE_REQUEST = 71

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImage = view.findViewById(R.id.profile_image_profile)  // match XML
        username = view.findViewById(R.id.username_in_profile)        // match XML
        userBio = view.findViewById(R.id.bio_profile)                 // match XML
        editProfileBtn = view.findViewById(R.id.edit_profile_Button)  // match XML
        recyclerView = view.findViewById(R.id.recyclerview_profile)   // match XML

        setupRecyclerView()

        setupRecyclerView()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users").child(uid)
        postsRef = database.getReference("Posts")
        storage = FirebaseStorage.getInstance()
        profilePicRef = storage.reference.child("profile_pictures").child("$uid.jpg")

        loadUserInfo()
        loadUserPosts()

        editProfileBtn.setOnClickListener {
            openImagePicker()
        }
    }

    private fun setupRecyclerView() {
        postAdapter = UserPostAdapter(requireContext(), postList)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = postAdapter
    }

    private fun loadUserInfo() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usernameStr = snapshot.child("username").getValue(String::class.java) ?: "User"
                val bioStr = snapshot.child("bio").getValue(String::class.java) ?: "No bio available"
                val profileUrl = snapshot.child("image").getValue(String::class.java)

                username.text = usernameStr
                userBio.text = bioStr

                Glide.with(requireContext())
                    .load(profileUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(profileImage)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadUserPosts() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        postsRef.orderByChild("publisher").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    for (postSnap in snapshot.children) {
                        val post = postSnap.getValue(Post::class.java)
                        post?.let { postList.add(it) }
                    }
                    postAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            val imageUri: Uri = data?.data ?: return
            uploadProfileImage(imageUri)
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val uploadTask = profilePicRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                updateProfileImageUrl(uri.toString())
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfileImageUrl(imageUrl: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userRef.child("image").setValue(imageUrl)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile image updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile image", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
