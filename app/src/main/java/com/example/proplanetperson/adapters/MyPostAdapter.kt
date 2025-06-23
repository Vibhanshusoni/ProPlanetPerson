package com.example.proplanetperson.adapters

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast // Added for potential Toast message in error handling
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.fragments.PostDetailFragment
import com.example.proplanetperson.R
import com.example.proplanetperson.models.Post // Ensure this import is correct for your Post data class
import com.squareup.picasso.Picasso

// KTX extension for SharedPreferences (good to have explicitly defined or from core-ktx)
// If this function is defined in multiple files, consider moving it to a separate utility file
// (e.g., SharedPreferencesExt.kt in a 'utils' or 'extensions' package) for better organization.
inline fun SharedPreferences.edit(
    commit: Boolean = false,
    action: SharedPreferences.Editor.() -> Unit
) {
    val editor = edit()
    action(editor)
    if (commit) {
        editor.commit()
    } else {
        editor.apply()
    }
}


class MyPostAdapter(private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<MyPostAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postedImg: ImageView

        init {
            // Ensure this ID matches your mypost_layout.xml
            postedImg = itemView.findViewById(R.id.my_posted_picture)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Ensure this layout exists and is designed for a single post item in a grid/list
        val view = LayoutInflater.from(mContext).inflate(R.layout.mypost_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = mPost[position]

        // Load image using Picasso
        // Assuming Post data class has a property 'imageUrl: String?'
        Picasso.get().load(post.imageUrl)
            .placeholder(R.drawable.ic_profile_placeholder) // Make sure this drawable exists
            .error(R.drawable.error_image) // IMPORTANT: Make sure you have an 'error_image' drawable (e.g., a broken image icon)
            .into(holder.postedImg)

        holder.postedImg.setOnClickListener {
            // Check if the context is a FragmentActivity, which is required for FragmentManager
            if (mContext is FragmentActivity) {
                // Get postId directly from the Post object
                // Assuming Post data class has a property 'postId: String?'
                val postId = post.postId ?: "" // Handle potential null postId with a default empty string

                if (postId.isNotEmpty()) {
                    // Store the postId in SharedPreferences for the PostDetailFragment to retrieve
                    // The KTX extension function for SharedPreferences.edit is correctly used here,
                    // and .apply() is called by default.
                    mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                        putString("postid", postId)
                    }

                    // Perform the fragment transaction to show PostDetailFragment
                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PostDetailFragment()) // R.id.fragment_container is your FrameLayout where fragments are loaded
                        .addToBackStack(null) // Allows going back to the previous fragment
                        .commit()
                } else {
                    Log.w("MyPostAdapter", "Attempted to click on a post with an empty PostId. Post: ${post}")
                    Toast.makeText(mContext, "Error: Post ID not found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Log an error if the context is not a FragmentActivity (e.g., if this adapter is used in a non-fragment-hosting Activity)
                Log.e("MyPostAdapter", "Context is not a FragmentActivity, cannot perform fragment transaction.")
                Toast.makeText(mContext, "Cannot open post details here.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}