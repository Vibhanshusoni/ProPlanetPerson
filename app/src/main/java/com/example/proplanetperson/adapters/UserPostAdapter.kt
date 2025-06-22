package com.example.proplanetperson.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences // Keep this import
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
// Removed androidx.annotation.NonNull as it's redundant in Kotlin
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.AddCommentActivity
import com.example.proplanetperson.R
import com.example.proplanetperson.ShowUsersActivity
import com.example.proplanetperson.fragments.PostDetailFragment
import com.example.proplanetperson.fragments.ProfileFragment
import com.example.proplanetperson.models.Post // Ensure this import is correct for your Post data class
import com.example.proplanetperson.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
// Import the KTX extension function for SharedPreferences.edit
import androidx.core.content.edit

class UserPostAdapter(
    private val mContext: Context,
    private val mPost: List<Post>
) : RecyclerView.Adapter<UserPostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mPost.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]

        // FIX: Use 'postid' (lowercase 'i') and handle nullability
        val postId = post.postId ?: ""
        // FIX: Use 'postimage' (lowercase 'i') and handle nullability
        val postImageUrl = post.postimage ?: ""

        Picasso.get()
            .load(postImageUrl) // Use the non-nullable postImageUrl
            .placeholder(R.drawable.ic_profile_placeholder) // Make sure you have this drawable
            .error(R.drawable.error_image) // Make sure you have this drawable
            .into(holder.postImage)

        // FIX: Use 'caption' and handle nullability
        holder.caption.text = post.caption ?: ""

        // FIX: Use 'publisher' and handle nullability
        val publisherId = post.publisher ?: ""
        publisherInfo(holder.profileImage, holder.username, holder.publisher, publisherId)

        // FIX: Pass non-nullable postId to functions
        isLiked(postId, holder.likeButton, holder.postImage)
        isSaved(postId, holder.saveButton)
        getCountOfLikes(postId, holder.likes)
        getComments(postId, holder.comments)

        val publisherClickListener = View.OnClickListener {
            if (publisherId.isNotEmpty()) { // Use the non-nullable publisherId
                // FIX: Use KTX extension for SharedPreferences.edit
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                    putString("profileid", publisherId)
                }
                if (mContext is FragmentActivity) {
                    mContext.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    Log.e("UserPostAdapter", "Context is not FragmentActivity for profile navigation.")
                }
            } else {
                Log.w("UserPostAdapter", "Publisher ID is empty for profile click.")
            }
        }

        holder.publisher.setOnClickListener(publisherClickListener)
        holder.profileImage.setOnClickListener(publisherClickListener)
        holder.username.setOnClickListener(publisherClickListener)

        holder.postImage.setOnClickListener {
            // FIX: Use KTX extension for SharedPreferences.edit
            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                putString("postid", postId) // Use the non-nullable postId
            }
            if (mContext is FragmentActivity) {
                mContext.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PostDetailFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                Log.e("UserPostAdapter", "Context is not FragmentActivity for post detail navigation.")
            }
        }

        holder.likeButton.setOnClickListener {
            firebaseUser?.let { currentUser -> // Use let for safe access to firebaseUser
                val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId) // Use non-nullable postId
                if (holder.likeButton.tag == "like") {
                    likesRef.child(currentUser.uid).setValue(true)
                    pushNotification(postId, publisherId) // Use non-nullable postId and publisherId
                } else {
                    likesRef.child(currentUser.uid).removeValue()
                }
            } ?: Toast.makeText(mContext, "Login required to like posts.", Toast.LENGTH_SHORT).show()
        }

        val commentClickListener = View.OnClickListener {
            val intent = Intent(mContext, AddCommentActivity::class.java).apply {
                putExtra("POST_ID", postId) // Use non-nullable postId
            }
            mContext.startActivity(intent)
        }

        holder.comments.setOnClickListener(commentClickListener)
        holder.commentButton.setOnClickListener(commentClickListener)

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java).apply {
                putExtra("id", postId) // Use non-nullable postId
                putExtra("title", "likes")
            }
            mContext.startActivity(intent)
        }

        holder.saveButton.setOnClickListener {
            firebaseUser?.let { currentUser -> // Use let for safe access to firebaseUser
                val savesRef = FirebaseDatabase.getInstance().reference.child("Saves").child(currentUser.uid)
                if (holder.saveButton.tag == "Save") {
                    savesRef.child(postId).setValue(true) // Use non-nullable postId
                    Toast.makeText(mContext, "Post Saved", Toast.LENGTH_SHORT).show()
                } else {
                    savesRef.child(postId).removeValue() // Use non-nullable postId
                    Toast.makeText(mContext, "Post Unsaved", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(mContext, "Login required to save posts.", Toast.LENGTH_SHORT).show()
        }
    }

    // FIX: Removed @NonNull from ViewHolder constructor
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
        FirebaseDatabase.getInstance().reference.child("Comment").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserPostAdapter", "getComments failed: ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    commentTextView.text = "View all ${snapshot.childrenCount} comments"
                }
            })
    }

    private fun pushNotification(postId: String, userId: String) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(userId)

        val notifyMap = hashMapOf<String, Any>(
            "userid" to currentUid,
            "text" to "♥ liked your post ♥",
            "postid" to postId,
            "ispost" to true
        )

        ref.push().setValue(notifyMap).addOnFailureListener {
            Log.e("UserPostAdapter", "Notification push failed: ${it.message}")
        }
    }

    private fun isLiked(postId: String, imageView: ImageView, postedImg: ImageView) {
        firebaseUser = FirebaseAuth.getInstance().currentUser ?: return

        FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(firebaseUser!!.uid).exists()) {
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
                    Log.e("UserPostAdapter", "isLiked failed: ${error.message}")
                }
            })
    }

    private fun getCountOfLikes(postId: String, likesText: TextView) {
        FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    likesText.text = "${snapshot.childrenCount} likes"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserPostAdapter", "getCountOfLikes failed: ${error.message}")
                }
            })
    }

    private fun isSaved(postId: String, imageView: ImageView) {
        firebaseUser = FirebaseAuth.getInstance().currentUser ?: return

        FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
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
                    Log.e("UserPostAdapter", "isSaved failed: ${error.message}")
                }
            })
    }

    private fun publisherInfo(
        profileImage: CircleImageView,
        username: TextView,
        publisher: TextView,
        publisherID: String
    ) {
        // FIX: Added a check for empty publisherID
        if (publisherID.isEmpty()) {
            Picasso.get().load(R.drawable.profile).into(profileImage)
            username.text = "Unknown"
            publisher.text = "Unknown"
            Log.w("UserPostAdapter", "Publisher ID is empty in publisherInfo.")
            return
        }

        FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        // FIX: Use user.image (assuming 'image' property in User class)
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
                    Log.e("UserPostAdapter", "publisherInfo failed: ${error.message}")
                    Picasso.get().load(R.drawable.profile).into(profileImage)
                    username.text = "Error"
                    publisher.text = "Error"
                }
            })
    }
}
