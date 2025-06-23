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
//import com.example.proplanetperson.MainActivity // Keep only if you intend to navigate here and then handle the ID
import com.example.proplanetperson.ui.UserProfileActivity // Import UserProfileActivity
import com.example.proplanetperson.models.Comment
import com.example.proplanetperson.models.User
import com.example.proplanetperson.R
import com.example.proplanetperson.utils.SessionManager // Assuming you might need it for some future logic, but not for getting current user here.
// Removed: import com.google.firebase.auth.FirebaseAuth
// Removed: import com.google.firebase.auth.FirebaseUser
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

    // No need for sessionManager here unless you're doing a comparison or specific action for the current user
    // private lateinit var sessionManager: SessionManager // Removed for this specific fix.

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var publisherUsername: TextView
        var commentText: TextView

        init {
            profileImage = itemView.findViewById(R.id.publisher_image_profile)
            publisherUsername = itemView.findViewById(R.id.publisher_username)
            commentText = itemView.findViewById(R.id.comment_text) // IMPORTANT: Make sure this ID is correct in your XML!
            // If it was publisher_comment_text, use that.
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
        // Removed: sessionManager = SessionManager getInstance().currentUser // This line was incorrect and unnecessary here.

        val comment = mComment[position]

        // Access data class properties directly (assuming Comment is a data class)
        if (comment.commentText.isNotEmpty()) { // Assuming property name is 'commentText'
            holder.commentText.text = comment.commentText
        } else {
            holder.commentText.text = ""
        }

        // Access publisher ID directly (assuming 'publisherId' property)
        publisherInfo(holder.profileImage, holder.publisherUsername, comment.publisherId) // Assuming property name is 'publisherId'

        // Set click listeners for publisher username and profile image
        val clickListener = View.OnClickListener {
            val publisherId = comment.publisherId // Access property directly
            if (publisherId.isNotEmpty()) {
                // Navigate to UserProfileActivity when clicking on publisher
                val intent = Intent(mContext, UserProfileActivity::class.java).apply {
                    putExtra("USER_ID", publisherId) // Pass the publisher's ID
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
            profileImage.setImageResource(R.drawable.profile) // Placeholder profile image
            usernameTextView.text = "Unknown User"
            return
        }

        // Assuming your 'users' node contains user data, and the ID matches the publisherID
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(publisherID) // Use "users" as your node name
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentAdapter", "Failed to load publisher info: ${error.message}", error.toException())
                profileImage.setImageResource(R.drawable.profile) // Placeholder profile image
                usernameTextView.text = "Error Loading User"
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java) // Assumes User is a data class

                    user?.let {
                        // Access User data class properties directly
                        Picasso.get().load(it.image) // Assuming User has a public 'image' property
                            .placeholder(R.drawable.profile) // Placeholder profile image
                            .error(R.drawable.profile) // Error profile image
                            .into(profileImage)

                        usernameTextView.text = it.username // Assuming User has a public 'username' property
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