package com.example.proplanetperson

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proplanetperson.adapters.CommentAdapter
import com.example.proplanetperson.models.Comment // Assuming you have this data class
import com.example.proplanetperson.models.User // Assuming you have this data class (as updated previously)
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.example.proplanetperson.databinding.ActivityAddCommentBinding
import android.util.Log // For logging errors

class AddCommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCommentBinding
    private var firebaseUser: FirebaseUser? = null
    private lateinit var commentAdapter: CommentAdapter // Use lateinit as it will be initialized
    private var commentList: MutableList<Comment> = ArrayList() // Initialize as an empty ArrayList

    // Declare postid as a class-level variable since it's used in multiple functions
    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.commentsToolbar)
        supportActionBar?.apply { // Use safe call for supportActionBar
            title = "Comments"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.commentsToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Modern way to handle back press
        }

        // Initialize RecyclerView
        val recyclerView = binding.recyclerviewComments
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize commentList and commentAdapter
        // No need for casting anymore since commentList is directly initialized as ArrayList
        commentAdapter = CommentAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        // Get current Firebase user
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            Toast.makeText(this, "You need to be logged in to comment.", Toast.LENGTH_SHORT).show()
            finish()
            return // Exit if user is not logged in
        }

        // Get post ID from intent
        // Using getExtra with default value to avoid NullPointerException if "POST_ID" is missing
        postId = intent.getStringExtra("POST_ID") ?: run {
            Toast.makeText(this, "Post ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load data
        getImage()
        readComments(postId)
        getPostImage(postId)

        // Set click listener for posting comments
        binding.postComment.setOnClickListener {
            val commentText = binding.addComment.text.toString().trim() // Trim whitespace
            if (commentText.isEmpty()) {
                Toast.makeText(this, "You can't send an empty comment.", Toast.LENGTH_SHORT).show()
            } else {
                postComment(postId, commentText)
            }
        }
    }

    private fun postComment(postId: String, commentText: String) {
        val currentUser = firebaseUser // Safe reference
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        val commentRef: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Comment").child(postId)

        val commentMap = HashMap<String, Any>()
        commentMap["publisher"] = currentUser.uid
        commentMap["comment"] = commentText // Use the trimmed comment text

        commentRef.push().setValue(commentMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    pushNotification(postId, commentText) // Pass commentText for notification
                    binding.addComment.setText("") // Clear the input field
                    Toast.makeText(this, "Comment posted successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to post comment: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddCommentActivity", "Error posting comment", task.exception)
                }
            }
    }

    private fun getImage() {
        val currentUser = firebaseUser
        if (currentUser == null) {
            Log.e("AddCommentActivity", "getImage called but firebaseUser is null.")
            return
        }

        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser.uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java) // Directly use User data class
                    user?.let { // Use safe call and let for user object
                        Picasso.get()
                            .load(it.image) // Access property directly: user.image
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile) // Add an error placeholder
                            .into(binding.userProfileImage)
                    }
                } else {
                    Log.d("AddCommentActivity", "User data not found for current user: ${currentUser.uid}")
                    // Optionally set a default image if user data is missing
                    binding.userProfileImage.setImageResource(R.drawable.profile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddCommentActivity", "Failed to load user image: ${error.message}", error.toException())
            }
        })
    }

    private fun pushNotification(postId: String, commentText: String) {
        val currentUser = firebaseUser
        if (currentUser == null) {
            Log.e("AddCommentActivity", "pushNotification called but firebaseUser is null.")
            return
        }

        // It seems notifications are added under a node unique to the commenting user.
        // If this is meant to be a general notification for the post owner,
        // you might need to fetch the post owner's UID first.
        // For now, assuming it's structured this way based on your original code.
        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(currentUser.uid)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userid"] = currentUser.uid
        notifyMap["text"] = "commented: $commentText" // Use string interpolation
        notifyMap["postid"] = postId
        notifyMap["ispost"] = true // Assuming this means it's a post-related notification

        ref.push().setValue(notifyMap)
            .addOnFailureListener { e ->
                Log.e("AddCommentActivity", "Failed to push notification", e)
            }
    }

    private fun readComments(postId: String) {
        val ref: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Comment").child(postId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { // Renamed p0 to snapshot for clarity
                commentList.clear() // Use the initialized commentList
                for (childSnapshot in snapshot.children) { // Renamed snapshot to childSnapshot
                    val comment: Comment? = childSnapshot.getValue(Comment::class.java)
                    comment?.let { // Safely add non-null comments
                        commentList.add(it)
                    }
                }
                commentAdapter.notifyDataSetChanged() // Use the initialized commentAdapter
                binding.recyclerviewComments.scrollToPosition(commentList.size - 1) // Scroll to latest comment
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddCommentActivity", "Failed to read comments: ${error.message}", error.toException())
            }
        })
    }

    private fun getPostImage(postId: String) {
        val postRef = FirebaseDatabase.getInstance()
            .reference.child("Posts").child(postId).child("postimage")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { // Renamed p0 to snapshot
                if (snapshot.exists()) {
                    val image = snapshot.value.toString()
                    Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile) // Add an error placeholder
                        .into(binding.postImageComment)
                } else {
                    Log.d("AddCommentActivity", "Post image not found for post ID: $postId")
                    // Optionally set a default image for the post if it doesn't exist
                    binding.postImageComment.setImageResource(R.drawable.profile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddCommentActivity", "Failed to get post image: ${error.message}", error.toException())
            }
        })
    }
}