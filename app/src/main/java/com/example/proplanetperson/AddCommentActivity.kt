package com.example.proplanetperson

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proplanetperson.adapters.CommentAdapter
import com.example.proplanetperson.api.ApiClient
import com.example.proplanetperson.api.PostRepositoryImpl
import com.example.proplanetperson.api.UserRepositoryImpl // Needed for PostViewModel factory
import com.example.proplanetperson.models.Comment
import com.example.proplanetperson.ui.post.PostViewModel // Import the new ViewModel
import com.example.proplanetperson.utils.Resource
import com.example.proplanetperson.utils.SessionManager
import com.example.proplanetperson.databinding.ActivityAddCommentBinding
import com.squareup.picasso.Picasso

class AddCommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCommentBinding
    private lateinit var sessionManager: SessionManager // Use SessionManager
    private lateinit var postViewModel: PostViewModel // Use the new PostViewModel
    private lateinit var commentAdapter: CommentAdapter
    private var commentList: MutableList<Comment> = ArrayList()

    private lateinit var postId: String
    private var currentUserId: String? = null
    private var authToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Get user ID and token from SessionManager
        currentUserId = sessionManager.getUserId()
        authToken = sessionManager.getAuthToken()

        if (currentUserId.isNullOrEmpty() || authToken.isNullOrEmpty()) {
            Toast.makeText(this, "You need to be logged in to comment.", Toast.LENGTH_SHORT).show()
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


        // Set up the toolbar
        setSupportActionBar(binding.commentsToolbar)
        supportActionBar?.apply {
            title = "Comments"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.commentsToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize RecyclerView
        val recyclerView = binding.recyclerviewComments
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        // Get post ID from intent
        postId = intent.getStringExtra("POST_ID") ?: run {
            Toast.makeText(this, "Post ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // --- Observe LiveData from ViewModel ---
        postViewModel.userProfileImage.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading on image if desired
                }
                is Resource.Success -> {
                    resource.data?.let { imageUrl ->
                        if (imageUrl.isNotEmpty()) {
                            Picasso.get().load(imageUrl).placeholder(R.drawable.profile).into(binding.userProfileImage)
                        } else {
                            binding.userProfileImage.setImageResource(R.drawable.profile)
                        }
                    } ?: binding.userProfileImage.setImageResource(R.drawable.profile)
                }
                is Resource.Error -> {
                    Log.e("AddCommentActivity", "Error loading user profile image: ${resource.message}")
                    binding.userProfileImage.setImageResource(R.drawable.profile) // Set default on error
                }
                is Resource.Idle -> { // ADDED THIS BRANCH
                    Log.d("AddCommentActivity", "User profile image state is idle.")
                }
            }
        }

        postViewModel.comments.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading indicator for comments if desired
                }
                is Resource.Success -> {
                    resource.data?.let { comments ->
                        commentList.clear()
                        commentList.addAll(comments)
                        commentAdapter.notifyDataSetChanged()
                        if (commentList.isNotEmpty()) {
                            binding.recyclerviewComments.scrollToPosition(commentList.size - 1)
                        }
                    }
                }
                is Resource.Error -> {
                    Log.e("AddCommentActivity", "Error loading comments: ${resource.message}")
                    Toast.makeText(this, "Failed to load comments: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Idle -> { // ADDED THIS BRANCH
                    Log.d("AddCommentActivity", "Comments state is idle.")
                }
            }
        }

        postViewModel.postImage.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading on post image if desired
                }
                is Resource.Success -> {
                    resource.data?.let { imageUrl ->
                        if (imageUrl.isNotEmpty()) {
                            Picasso.get().load(imageUrl).placeholder(R.drawable.profile).into(binding.postImageComment)
                        } else {
                            binding.postImageComment.setImageResource(R.drawable.profile)
                        }
                    } ?: binding.postImageComment.setImageResource(R.drawable.profile)
                }
                is Resource.Error -> {
                    Log.e("AddCommentActivity", "Error loading post image: ${resource.message}")
                    binding.postImageComment.setImageResource(R.drawable.profile) // Set default on error
                }
                is Resource.Idle -> { // ADDED THIS BRANCH
                    Log.d("AddCommentActivity", "Post image state is idle.")
                }
            }
        }

        postViewModel.postCommentResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.postComment.isEnabled = false // Disable post button
                    Toast.makeText(this, "Posting comment...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    binding.postComment.isEnabled = true // Re-enable
                    binding.addComment.setText("") // Clear input
                    Toast.makeText(this, "Comment posted successfully!", Toast.LENGTH_SHORT).show()
                    // Re-fetch comments to show the new one
                    currentUserId?.let { userId ->
                        authToken?.let { token ->
                            postViewModel.getComments(postId, token)
                        }
                    }
                    postViewModel.resetPostCommentResult() // Reset the state
                }
                is Resource.Error -> {
                    binding.postComment.isEnabled = true // Re-enable
                    Toast.makeText(this, "Failed to post comment: ${resource.message}", Toast.LENGTH_LONG).show()
                    Log.e("AddCommentActivity", "Error posting comment: ${resource.message}")
                    postViewModel.resetPostCommentResult() // Reset the state
                }
                is Resource.Idle -> { // ADDED THIS BRANCH
                    Log.d("AddCommentActivity", "Post comment result state is idle.")
                }
            }
        }

        // --- Initiate data loading via ViewModel ---
        currentUserId?.let { userId ->
            authToken?.let { token ->
                postViewModel.getCurrentUserProfileImage(userId, token) // For user profile image in add comment bar
                postViewModel.getComments(postId, token)
                postViewModel.getPostImage(postId, token)
            }
        }

        // Set click listener for posting comments
        binding.postComment.setOnClickListener {
            val commentText = binding.addComment.text.toString().trim()
            if (commentText.isEmpty()) {
                Toast.makeText(this, "You can't send an empty comment.", Toast.LENGTH_SHORT).show()
            } else {
                currentUserId?.let { userId ->
                    authToken?.let { token ->
                        postViewModel.postComment(postId, token, userId, commentText)
                    }
                } ?: Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}