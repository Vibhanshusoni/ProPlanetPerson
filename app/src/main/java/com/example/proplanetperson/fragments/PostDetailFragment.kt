package com.example.proplanetperson.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.adapters.PostAdapter
import com.example.proplanetperson.models.Post
import com.example.proplanetperson.R
import com.google.firebase.database.*

class PostDetailFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var postid: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_detail, container, false)

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        postid = pref?.getString("postid", "none") // "none" is a valid default

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerview_postdetail)
        recyclerView.layoutManager = LinearLayoutManager(context)

        postList = ArrayList()
        // Pass null for the lambda as this adapter instance doesn't need to handle comment clicks
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>, null) }
        recyclerView.adapter = postAdapter

        readPosts(postid)

        return view
    }

    private fun readPosts(postid: String?) {
        // Handle null or "none" postid gracefully
        if (postid.isNullOrEmpty() || postid == "none") {
            Log.e("PostDetailFragment", "Post ID is null or empty/none. Cannot read posts.")
            return
        }

        // IMPORTANT: Ensure your Firebase Realtime Database path for posts is "Posts" (capital P)
        // If it's "posts" (lowercase p), change it here.
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postid) // Removed !!
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("PostDetailFragment", "Database error in readPosts: ${error.message}", error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear() // Safe call
                val post: Post? = snapshot.getValue(Post::class.java)
                post?.let { // Use let for safe access
                    postList?.add(it) // Safe call
                }
                postAdapter?.notifyDataSetChanged() // Safe call
            }
        })
    }
}
