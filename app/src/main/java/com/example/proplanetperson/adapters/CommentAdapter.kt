package com.example.proplanetperson.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.MainActivity
import com.example.proplanetperson.models.Comment
import com.example.proplanetperson.models.User // Still assume User is a data class with public properties or has getters
import com.example.proplanetperson.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(
    private var mContext: Context,
    private var mComment: List<Comment>
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var publisherUsername: TextView // Renamed for clarity
        var commentText: TextView       // Renamed for clarity

        init {
            // Ensure these IDs match your comment_item_layout.xml
            profileImage = itemView.findViewById(R.id.publisher_image_profile)
            publisherUsername = itemView.findViewById(R.id.publisher_username)
            commentText = itemView.findViewById(R.id.publisher_comment_text) // Make sure your XML uses this ID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mComment.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val comment = mComment[position]

        // FIX: Use getComment() method as 'comment' property is private
        if (comment.getComment().isNotEmpty()) {
            holder.commentText.text = comment.getComment()
        } else {
            holder.commentText.text = ""
        }

        // FIX: Use getPublisher() method as 'publisher' property is private
        publisherInfo(holder.profileImage, holder.publisherUsername, comment.getPublisher())

        // Set click listeners for publisher username and profile image
        val clickListener = View.OnClickListener {
            // FIX: Use getPublisher() method
            val publisherId = comment.getPublisher()
            if (publisherId.isNotEmpty()) {
                val intent = Intent(mContext, MainActivity::class.java).apply {
                    putExtra("PUBLISHER_ID", publisherId)
                }
                mContext.startActivity(intent)
            } else {
                Log.w("CommentAdapter", "Attempted to click with empty publisher ID.")
            }
        }

        holder.publisherUsername.setOnClickListener(clickListener)
        holder.profileImage.setOnClickListener(clickListener)
    }

    private fun publisherInfo(profileImage: CircleImageView, usernameTextView: TextView, publisherID: String) {
        if (publisherID.isEmpty()) {
            Log.w("CommentAdapter", "Publisher ID is empty, cannot fetch user info.")
            profileImage.setImageResource(R.drawable.profile)
            usernameTextView.text = "Unknown User"
            return
        }

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentAdapter", "Failed to load publisher info: ${error.message}", error.toException())
                profileImage.setImageResource(R.drawable.profile)
                usernameTextView.text = "Error Loading User"
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java) // Assumes User is a data class with public properties or has getters

                    user?.let {
                        // Assuming User is a data class with public 'image' and 'username' properties
                        // If User also has private properties, you'd need user.getImage() and user.getUsername()
                        Picasso.get().load(it.image) // FIX: if user.image is private, change to it.getImage()
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(profileImage)

                        usernameTextView.text = it.username // FIX: if user.username is private, change to it.getUsername()
                    } ?: run {
                        Log.w("CommentAdapter", "User data found but object is null for UID: $publisherID")
                        profileImage.setImageResource(R.drawable.profile)
                        usernameTextView.text = "User Not Found"
                    }
                } else {
                    Log.d("CommentAdapter", "User data not found for UID: $publisherID")
                    profileImage.setImageResource(R.drawable.profile)
                    usernameTextView.text = "User Removed"
                }
            }
        })
    }
}