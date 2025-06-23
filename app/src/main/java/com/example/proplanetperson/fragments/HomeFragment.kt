package com.example.proplanetperson.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log for better error logging
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel // Import ViewModel
import androidx.lifecycle.ViewModelProvider // Import ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import com.example.proplanetperson.models.Post
import com.example.proplanetperson.AddCommentActivity // Assuming this is for going to comment activity
import com.example.proplanetperson.R
import com.example.proplanetperson.adapters.PostAdapter
import com.example.proplanetperson.api.ApiClient // Import ApiClient
import com.example.proplanetperson.api.PostRepositoryImpl // Import PostRepositoryImpl
import com.example.proplanetperson.api.QuoteRepositoryImpl // Import QuoteRepositoryImpl
import com.example.proplanetperson.api.UserRepositoryImpl // Import UserRepositoryImpl (if PostViewModel needs it)
import com.example.proplanetperson.models.Quote // Ensure this import is correct for your Quote data class
import com.example.proplanetperson.ui.home.HomeViewModel // NEW: Import HomeViewModel
import com.example.proplanetperson.utils.Resource // Import Resource class

// Removed Firebase Database KTX imports as direct usage is moved to ViewModel
// import com.google.firebase.database.ktx.database
// import com.google.firebase.ktx.Firebase // Required for Firebase.database

class HomeFragment : Fragment() {

    private lateinit var quoteText: TextView
    private lateinit var quoteAuthor: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var natureImage: ImageView
    private lateinit var postRecyclerView: RecyclerView

    private lateinit var homeViewModel: HomeViewModel // NEW: Declare HomeViewModel
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>() // Moved here to be clear about its scope

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

        // Initialize HomeViewModel
        val postRepository = PostRepositoryImpl(ApiClient.postApi)
        val quoteRepository = QuoteRepositoryImpl(ApiClient.quoteApi)
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return HomeViewModel(postRepository, quoteRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        homeViewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        // Setup RecyclerView with onCommentClick handler
        // PostAdapter needs UserRepository if it's fetching user profile images within itself
        val userRepositoryForPostAdapter = UserRepositoryImpl(ApiClient.userApi) // Provide UserRepository
        postAdapter = PostAdapter(requireContext(), postList) // No onCommentClick handler in PostAdapter constructor anymore.
        // Assuming your PostAdapter doesn't take onCommentClick in constructor.
        // If it does, you'd pass a lambda here to open AddCommentActivity.
        // I am assuming the PostAdapter is just for display, and individual buttons handle clicks.
        // Let's revert the PostAdapter constructor. If you explicitly want the lambda in PostAdapter
        // for comment click, we can adjust that. For now, matching the standard PostAdapter.

        postRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postRecyclerView.adapter = postAdapter

        // --- Observe LiveData from HomeViewModel ---
        homeViewModel.quote.observe(viewLifecycleOwner) { resource ->
            swipeRefreshLayout.isRefreshing = false // Stop refreshing whether success or error
            when (resource) {
                is Resource.Loading -> {
                    // Optionally show a placeholder or loading state for quote
                }
                is Resource.Success -> {
                    resource.data?.let { quote ->
                        quoteText.text = "\"${quote.content}\""
                        quoteAuthor.text = "- ${quote.author ?: "Unknown"}"
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Failed to load quote: ${resource.message}", Toast.LENGTH_SHORT).show()
                    Log.e("HomeFragment", "Quote load error: ${resource.message}")
                }
                is Resource.Idle -> {
                    // Do nothing or reset UI
                }
            }
        }

        homeViewModel.posts.observe(viewLifecycleOwner) { resource ->
            swipeRefreshLayout.isRefreshing = false // Stop refreshing whether success or error
            when (resource) {
                is Resource.Loading -> {
                    // Optionally show a loading indicator for posts
                }
                is Resource.Success -> {
                    resource.data?.let { posts ->
                        postList.clear()
                        postList.addAll(posts)
                        postAdapter.notifyDataSetChanged()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Failed to load posts: ${resource.message}", Toast.LENGTH_SHORT).show()
                    Log.e("HomeFragment", "Posts load error: ${resource.message}")
                }
                is Resource.Idle -> {
                    // Do nothing or reset UI
                }
            }
        }


        // Swipe-to-refresh: Calls ViewModel to load data
        swipeRefreshLayout.setOnRefreshListener {
            homeViewModel.loadQuote()
            homeViewModel.loadPosts()
        }

        // Load a random image from Unsplash (still direct Coil load as it's UI specific)
        natureImage.load("https://source.unsplash.com/600x400/?nature,environment") {
            crossfade(true)
            placeholder(R.drawable.eco_icon)
            error(R.drawable.ic_eco)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initial load calls ViewModel to load data
        homeViewModel.loadQuote()
        homeViewModel.loadPosts()
    }
}