package com.example.proplanetperson.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import com.example.proplanetperson.models.Post
import com.example.proplanetperson.CommentActivity
import com.example.proplanetperson.R
import com.example.proplanetperson.adapters.PostAdapter
import com.example.proplanetperson.api.RetrofitInstance
import com.example.proplanetperson.models.Quote // Ensure this import is correct for your Quote data class
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Import the Kotlin extension for Firebase Database
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase // Required for Firebase.database

class HomeFragment : Fragment() {

    private lateinit var quoteText: TextView
    private lateinit var quoteAuthor: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var natureImage: ImageView
    private lateinit var postRecyclerView: RecyclerView

    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize UI components
        quoteText = view.findViewById(R.id.quoteText)
        quoteAuthor = view.findViewById(R.id.quoteAuthor)
        natureImage = view.findViewById(R.id.natureImage)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)
        postRecyclerView = view.findViewById(R.id.recycler_posts)

        // Setup RecyclerView with onCommentClick handler
        postAdapter = PostAdapter(requireContext(), postList) { post ->
            val intent = Intent(requireContext(), CommentActivity::class.java)
            intent.putExtra("postUrl", post.postimage)
            intent.putExtra("caption", post.caption)
            startActivity(intent)
        }
        postRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postRecyclerView.adapter = postAdapter

        // Swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadQuote()
            loadPosts()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Firebase Database reference here, after the view has been created
        databaseRef = Firebase.database.reference.child("posts")

        // Initial load (moved here to ensure databaseRef is initialized)
        loadQuote()
        loadPosts()
    }

    private fun loadQuote() {
        // Changed to call getRandomQuote() which returns a single Quote object
        RetrofitInstance.api.getRandomQuote().enqueue(object : Callback<Quote> { // Changed List<Quote> to Quote
            override fun onResponse(call: Call<Quote>, response: Response<Quote>) { // Changed List<Quote> to Quote
                swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val quote = response.body() // Now it's a single Quote object
                    quote?.let {
                        quoteText.text = "\"${it.content}\"" // Access 'content' property
                        quoteAuthor.text = "- ${it.author ?: "Unknown"}" // Access 'author' property
                    }
                } else {
                    Toast.makeText(requireContext(), "Something went wrong: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Quote>, t: Throwable) { // Changed List<Quote> to Quote
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(requireContext(), "Failed to load quote: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Load a random image from Unsplash
        natureImage.load("https://source.unsplash.com/600x400/?nature,environment") {
            crossfade(true)
            placeholder(R.drawable.eco_icon)
            error(R.drawable.ic_eco)
        }
    }

    private fun loadPosts() {
        swipeRefreshLayout.isRefreshing = true

        databaseRef.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let { postList.add(it) }
                }
                postList.reverse() // Show newest first
                postAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
