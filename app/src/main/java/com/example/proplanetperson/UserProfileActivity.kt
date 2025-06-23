package com.example.proplanetperson.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proplanetperson.R
import com.example.proplanetperson.models.Post
import com.example.proplanetperson.adapters.PostAdapter
import com.example.proplanetperson.utils.SessionManager
import com.google.firebase.database.*

class UserProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var profileUsername: TextView // This is correct
    private lateinit var profileBio: TextView
    private lateinit var followersCount: TextView
    private lateinit var followingCount: TextView
    private lateinit var profilePostsRecyclerView: RecyclerView

    private lateinit var database: DatabaseReference
    private lateinit var sessionManager: SessionManager

    private lateinit var userId: String
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Initialize views
        profileImageView = findViewById(R.id.profileImageView)
        profileUsername = findViewById(R.id.profileUsername) // CORRECTED THIS LINE
        profileBio = findViewById(R.id.profileBio) // This was already correct
        followersCount = findViewById(R.id.followersCount)
        followingCount = findViewById(R.id.followingCount)
        profilePostsRecyclerView = findViewById(R.id.profilePostsRecyclerView)

        // Firebase init
        sessionManager = SessionManager(this)
        database = FirebaseDatabase.getInstance().reference

        // Get user ID from intent or from SessionManager
        userId = intent.getStringExtra("USER_ID") ?: run {
            sessionManager.getUserId()
        } ?: ""

        // IMPORTANT: Check if userId is still empty. If so, we cannot load a profile.
        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Cannot load profile.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        loadUserProfile()
        loadUserPosts()
    }

    private fun setupRecyclerView() {
        profilePostsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        postAdapter = PostAdapter(this, postList)
        profilePostsRecyclerView.adapter = postAdapter
    }

    private fun loadUserProfile() {
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java) ?: "Unknown"
                val bio = snapshot.child("bio").getValue(String::class.java) ?: ""
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                profileUsername.text = username
                profileBio.text = bio

                Glide.with(this@UserProfileActivity)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(profileImageView)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserProfileActivity, "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("UserProfileActivity", "Failed to load profile", error.toException())
            }
        })

        // Load follower/following counts
        database.child("Follow").child(userId).child("followers")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    followersCount.text = snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfileActivity", "Failed to load followers count", error.toException())
                }
            })

        database.child("Follow").child(userId).child("following")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    followingCount.text = snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfileActivity", "Failed to load following count", error.toException())
                }
            })
    }

    private fun loadUserPosts() {
        database.child("posts")
            .orderByChild("uid")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    for (postSnap in snapshot.children) {
                        val post = postSnap.getValue(Post::class.java)
                        post?.let { postList.add(it) }
                    }
                    postList.reverse()
                    postAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfileActivity", "Failed to load posts", error.toException())
                }
            })
    }
}