package com.example.proplanetperson.adapters

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.fragments.PostDetailFragment
import com.example.proplanetperson.R
import com.example.proplanetperson.models.Post // Ensure this import is correct for your Post data class
import com.squareup.picasso.Picasso

// KTX extension for SharedPreferences (often part of androidx.core.content.edit, but can be defined if not available)
// If you get an error here, you might need to add 'androidx.core:core-ktx:<latest_version>' to your build.gradle.kts
// and ensure you have the correct import for it.
// For now, we'll assume it's implicitly available or you'll add the core-ktx dependency.
// Or, you can define it manually if you prefer:

// FIX: Uncommenting and including this extension function to ensure it's available
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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { // Removed @NonNull
        var postedImg: ImageView

        init {
            postedImg = itemView.findViewById(R.id.my_posted_picture)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.mypost_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = mPost[position]

        // FIX 1: Access properties directly (post.imageUrl) instead of getPostImage()
        Picasso.get().load(post.imageUrl)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.error_image) // Make sure you have an 'error_image' drawable
            .into(holder.postedImg)

        holder.postedImg.setOnClickListener {
            if (mContext is FragmentActivity) {
                // FIX 2: Access properties directly (post.postId) instead of getPostId()
                val postId = post.postId ?: "" // Handle potential null postId with a default empty string
                if (postId.isNotEmpty()) {
                    // FIX 3: Use KTX extension function for SharedPreferences.edit
                    mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                        putString("postid", postId)
                        // apply() is called by default with the KTX extension unless commit = true
                    }

                    mContext.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PostDetailFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    Log.w("MyPostAdapter", "Attempted to click on a post with an empty PostId.")
                }
            } else {
                Log.e("MyPostAdapter", "Context is not a FragmentActivity, cannot perform fragment transaction.")
                // Consider adding a Toast here for the user, e.g.:
                // Toast.makeText(mContext, "Cannot open post details here.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
