package com.example.proplanetperson.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proplanetperson.api.PostRepository // Import PostRepository
import com.example.proplanetperson.api.QuoteRepository // Import QuoteRepository
import com.example.proplanetperson.models.Post
import com.example.proplanetperson.models.Quote
import com.example.proplanetperson.utils.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class HomeViewModel(
    private val postRepository: PostRepository, // Inject PostRepository
    private val quoteRepository: QuoteRepository // Inject QuoteRepository
) : ViewModel() {

    private val _quote = MutableLiveData<Resource<Quote>>()
    val quote: LiveData<Resource<Quote>> = _quote

    private val _posts = MutableLiveData<Resource<List<Post>>>()
    val posts: LiveData<Resource<List<Post>>> = _posts

    // Firebase Database Reference for posts (still directly here for now as PostRepository doesn't abstract it fully yet)
    private val databaseRef = FirebaseDatabase.getInstance().reference.child("posts")

    fun loadQuote() {
        _quote.value = Resource.Loading()
        viewModelScope.launch {
            _quote.value = quoteRepository.getRandomQuote()
        }
    }

    fun loadPosts() {
        _posts.value = Resource.Loading()
        viewModelScope.launch {
            // This part still uses direct Firebase Realtime Database.
            // Ideally, PostRepository would abstract this, but given its current methods,
            // we'll keep it here for now until a full Firebase Realtime Database abstraction
            // is introduced in PostRepository.
            databaseRef.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val postList = mutableListOf<Post>()
                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(Post::class.java)
                        post?.let { postList.add(it) }
                    }
                    postList.reverse() // Show newest first
                    _posts.value = Resource.Success(postList)
                }

                override fun onCancelled(error: DatabaseError) {
                    _posts.value = Resource.Error("Failed to load posts: ${error.message}")
                }
            })
        }
    }

    // Reset functions for LiveData states
    fun resetQuoteState() {
        _quote.value = Resource.Idle()
    }

    fun resetPostsState() {
        _posts.value = Resource.Idle()
    }
}