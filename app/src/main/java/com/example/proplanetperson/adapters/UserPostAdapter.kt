package com.example.proplanetperson.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
// Removed androidx.annotation.NonNull as it's redundant in Kotlin
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.AddCommentActivity
import com.example.proplanetperson.R
import com.example.proplanetperson.ShowUsersActivity
import com.example.proplanetperson.fragments.PostDetailFragment
import com.example.proplanetperson.fragments.ProfileFragment
import com.example.proplanetperson.models.Post // Ensure this import is correct for your Post data class (should have imageUrl, uid)
import com.example.proplanetperson.models.User // Ensure this import is correct for your User data class
// Removed Firebase Auth imports
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.proplanetperson.utils.SessionManager // NEW: Import SessionManager
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import androidx.core.content.edit // Import the KTX extension function for SharedPreferences.edit

class UserPostAdapter(
    private val mContext: Context,
    private val mPost: List<Post>
) : RecyclerView.Adapter<UserPostAdapter.ViewHolder>() {

    // Removed firebaseUser, now using SessionManager
    private lateinit var sessionManager: SessionManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Initialize SessionManager here, it's safe as Context is available
        sessionManager = SessionManager(mContext)

        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mPost.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // No longer getting firebaseUser here.
        val post = mPost[position]
        val currentUserId = sessionManager.getUserId() // Get current user ID from SessionManager

        // Use properties from the corrected Post data class (postId, imageUrl, caption, uid)
        val postId = post.postId
        val postImageUrl = post.imageUrl // Use post.imageUrl
        val captionText = post.caption
        val publisherId = post.uid // Use post.uid

        Picasso.get()
            .load(postImageUrl) // Use the non-nullable postImageUrl
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.error_image)
            .into(holder.postImage)

        holder.caption.text = captionText

        publisherInfo(holder.profileImage, holder.username, holder.publisher, publisherId)

        // Pass currentUserId to relevant functions
        isLiked(postId, holder.likeButton, holder.postImage, currentUserId)
        isSaved(postId, holder.saveButton, currentUserId)
        getCountOfLikes(postId, holder.likes)
        getComments(postId, holder.comments)

        val publisherClickListener = View.OnClickListener {
            if (publisherId.isNotEmpty()) {
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                    putString("profileid", publisherId)
                }
                if (mContext is FragmentActivity) {
                    mContext.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .addToBackStack(null) // Optional: Add to back stack
                        .commit()
                } else {
                    Log.e("UserPostAdapter", "Context is not FragmentActivity for profile navigation.")
                    Toast.makeText(mContext, "Cannot open profile.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.w("UserPostAdapter", "Publisher ID is empty for profile click.")
                Toast.makeText(mContext, "Publisher ID missing.", Toast.LENGTH_SHORT).show()
            }
        }

        holder.publisher.setOnClickListener(publisherClickListener)
        holder.profileImage.setOnClickListener(publisherClickListener)
        holder.username.setOnClickListener(publisherClickListener)

        holder.postImage.setOnClickListener {
            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                putString("postid", postId)
            }
            if (mContext is FragmentActivity) {
                mContext.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PostDetailFragment())
                    .addToBackStack(null) // Optional: Add to back stack
                    .commit()
            } else {
                Log.e("UserPostAdapter", "Context is not FragmentActivity for post detail navigation.")
                Toast.makeText(mContext, "Cannot open post details.", Toast.LENGTH_SHORT).show()
            }
        }

        holder.likeButton.setOnClickListener {
            currentUserId?.let { uid -> // Use let for safe access to currentUserId
                val likesRef = FirebaseDatabase.getInstance().reference.child("likes").child(postId) // Consistent lowercase "likes"
                if (holder.likeButton.tag == "like") {
                    likesRef.child(uid).setValue(true)
                    pushNotification(postId, publisherId) // Use non-nullable postId and publisherId
                } else {
                    likesRef.child(uid).removeValue()
                }
            } ?: Toast.makeText(mContext, "Login required to like posts.", Toast.LENGTH_SHORT).show()
        }

        val commentClickListener = View.OnClickListener {
            val intent = Intent(mContext, AddCommentActivity::class.java).apply {
                putExtra("POST_ID", postId)
            }
            mContext.startActivity(intent)
        }

        holder.comments.setOnClickListener(commentClickListener)
        holder.commentButton.setOnClickListener(commentClickListener)

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java).apply {
                putExtra("id", postId)
                putExtra("title", "likes")
            }
            mContext.startActivity(intent)
        }

        holder.saveButton.setOnClickListener {
            currentUserId?.let { uid -> // Use let for safe access to currentUserId
                val savesRef = FirebaseDatabase.getInstance().reference.child("saves").child(uid) // Consistent lowercase "saves"
                if (holder.saveButton.tag == "Save") {
                    savesRef.child(postId).setValue(true)
                    Toast.makeText(mContext, "Post Saved", Toast.LENGTH_SHORT).show()
                } else {
                    savesRef.child(postId).removeValue()
                    Toast.makeText(mContext, "Post Unsaved", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(mContext, "Login required to save posts.", Toast.LENGTH_SHORT).show()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.publisher_profile_image_post)
        val postImage: ImageView = itemView.findViewById(R.id.post_image_home)
        val likeButton: ImageView = itemView.findViewById(R.id.post_image_like_btn)
        val commentButton: ImageView = itemView.findViewById(R.id.post_image_comment_btn)
        val saveButton: ImageView = itemView.findViewById(R.id.post_save_comment_btn)
        val likes: TextView = itemView.findViewById(R.id.likes)
        val comments: TextView = itemView.findViewById(R.id.comments)
        val username: TextView = itemView.findViewById(R.id.publisher_user_name_post)
        val publisher: TextView = itemView.findViewById(R.id.publisher)
        val caption: TextView = itemView.findViewById(R.id.caption)
    }

    private fun getComments(postId: String, commentTextView: TextView) {
        FirebaseDatabase.getInstance().reference.child("comment").child(postId) // Consistent lowercase "comment"
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserPostAdapter", "getComments failed: ${error.message}", error.toException())
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    commentTextView.text = "View all ${snapshot.childrenCount} comments"
                }
            })
    }

    private fun pushNotification(postId: String, userId: String) {
        val currentUid = sessionManager.getUserId() // Use SessionManager
        if (currentUid.isNullOrEmpty()) {
            Log.w("UserPostAdapter", "Current user is null or empty, cannot push notification.")
            return
        }
        val ref = FirebaseDatabase.getInstance().reference.child("notification").child(userId) // Consistent lowercase "notification"

        val notifyMap = hashMapOf<String, Any>(
            "userid" to currentUid,
            "text" to "♥ liked your post ♥",
            "postid" to postId,
            "ispost" to true
        )

        ref.push().setValue(notifyMap).addOnFailureListener { e ->
            Log.e("UserPostAdapter", "Notification push failed: ${e.message}", e)
        }
    }

    private fun isLiked(postId: String, imageView: ImageView, postedImg: ImageView, currentUserId: String?) {
        if (currentUserId.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.heart_not_clicked)
            imageView.tag = "like"
            postedImg.tag = "like"
            return
        }

        FirebaseDatabase.getInstance().reference.child("likes").child(postId) // Consistent lowercase "likes"
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(currentUserId).exists()) {
                        imageView.setImageResource(R.drawable.heart_clicked)
                        imageView.tag = "liked"
                        postedImg.tag = "liked"
                    } else {
                        imageView.setImageResource(R.drawable.heart_not_clicked)
                        imageView.tag = "like"
                        postedImg.tag = "like"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserPostAdapter", "isLiked failed: ${error.message}", error.toException())
                }
            })
    }

    private fun getCountOfLikes(postId: String, likesText: TextView) {
        FirebaseDatabase.getInstance().reference.child("likes").child(postId) // Consistent lowercase "likes"
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    likesText.text = "${snapshot.childrenCount} likes"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserPostAdapter", "getCountOfLikes failed: ${error.message}", error.toException())
                }
            })
    }

    private fun isSaved(postId: String, imageView: ImageView, currentUserId: String?) {
        if (currentUserId.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.save_post_unfilled)
            imageView.tag = "Save"
            return
        }

        FirebaseDatabase.getInstance().reference.child("saves").child(currentUserId) // Consistent lowercase "saves"
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(postId).exists()) {
                        imageView.setImageResource(R.drawable.saved_post_filled)
                        imageView.tag = "Saved"
                    } else {
                        imageView.setImageResource(R.drawable.save_post_unfilled)
                        imageView.tag = "Save"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserPostAdapter", "isSaved failed: ${error.message}", error.toException())
                }
            })
    }

    private fun publisherInfo(
        profileImage: CircleImageView,
        username: TextView,
        publisher: TextView,
        publisherID: String
    ) {
        if (publisherID.isEmpty()) {
            Picasso.get().load(R.drawable.profile).into(profileImage)
            username.text = "Unknown"
            publisher.text = "Unknown"
            Log.w("UserPostAdapter", "Publisher ID is empty in publisherInfo.")
            return
        }

        // Consistent lowercase "users"
        FirebaseDatabase.getInstance().reference.child("users").child(publisherID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        Picasso.get().load(user.image).placeholder(R.drawable.profile).into(profileImage)
                        username.text = user.username
                        publisher.text = user.username
                    } else {
                        Log.d("UserPostAdapter", "User data not found for ID: $publisherID")
                        Picasso.get().load(R.drawable.profile).into(profileImage)
                        username.text = "User Not Found"
                        publisher.text = "User Not Found"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserPostAdapter", "publisherInfo failed: ${error.message}", error.toException())
                    Picasso.get().load(R.drawable.profile).into(profileImage)
                    username.text = "Error"
                    publisher.text = "Error"
                }
            })
    }
}