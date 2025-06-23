package com.example.proplanetperson.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.AddCommentActivity
import com.example.proplanetperson.fragments.PostDetailFragment
import com.example.proplanetperson.fragments.ProfileFragment
import com.example.proplanetperson.models.User // Ensure this import is correct for your User data class
import com.example.proplanetperson.models.Post // This Post data class should now have 'imageUrl' and 'uid'
import com.example.proplanetperson.R
import com.example.proplanetperson.ShowUsersActivity
// Removed Firebase Auth imports
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import androidx.core.content.edit // This import is crucial for the SharedPreferences.edit { } syntax
import com.example.proplanetperson.utils.SessionManager // NEW: Import SessionManager

// Added onCommentClick lambda to the constructor to handle comment button clicks from HomeFragment
class PostAdapter(
    private val mContext: Context,
    private val mPost: List<Post>,
    private val onCommentClick: ((Post) -> Unit)? = null // Nullable lambda for optional handling
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    // Removed FirebaseUser, now using SessionManager
    private lateinit var sessionManager: SessionManager // Declare SessionManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Initialize SessionManager here, it's safe as Context is available
        sessionManager = SessionManager(mContext)

        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    // code for events
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // No longer getting firebaseUser here.
        // User ID will be fetched via sessionManager.getUserId() when needed.

        val post = mPost[position]
        // IMPORTANT: Using 'post.postId', 'post.imageUrl', 'post.caption', 'post.uid'
        // based on the corrected Post data class definition.

        val postId = post.postId // Accessing post.postId directly
        val currentUserId = sessionManager.getUserId() // Get current user ID from SessionManager

        Picasso.get().load(post.imageUrl) // Direct access to imageUrl
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.error_image)
            .into(holder.postImage)

        holder.caption.text = post.caption // Direct access to caption
        // Pass post.uid as publisherID
        publisherInfo(holder.profileImage, holder.username, holder.publisher, post.uid)

        // Pass currentUserId for like/save checks
        isLiked(postId, holder.likeButton, holder.postImage, currentUserId)
        isSaved(postId, holder.saveButton, currentUserId)

        getCountofLikes(postId, holder.likes)
        getComments(postId, holder.comments)

        // --- Consolidated and Corrected Click Listeners ---

        // Listener for publisher text and profile image (common action: view publisher's profile)
        val publisherClickListener = View.OnClickListener {
            val publisherId = post.uid // Direct access to uid
            if (publisherId.isNotEmpty()) {
                // Using KTX extension for SharedPreferences.edit
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                    putString("profileid", publisherId)
                }

                if (mContext is FragmentActivity) {
                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .addToBackStack(null) // Optional: Add to back stack
                        .commit()
                } else {
                    Log.e("PostAdapter", "Context is not a FragmentActivity for profile navigation.")
                }
            } else {
                Log.w("PostAdapter", "Publisher ID is empty for profile click.")
                Toast.makeText(mContext, "Error: Publisher ID missing.", Toast.LENGTH_SHORT).show()
            }
        }

        holder.publisher.setOnClickListener(publisherClickListener)
        holder.profileImage.setOnClickListener(publisherClickListener)
        holder.username.setOnClickListener(publisherClickListener)


        // Listener for post image (different actions: view post details on single tap)
        holder.postImage.setOnClickListener {
            // Using KTX extension for SharedPreferences.edit
            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                putString("postid", postId) // Use the 'postId' variable
            }

            if (mContext is FragmentActivity) {
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, PostDetailFragment())
                    .addToBackStack(null) // Optional: Add to back stack
                    .commit()
            } else {
                Log.e("PostAdapter", "Context is not a FragmentActivity for post detail navigation.")
            }
        }


        // Listener for the like button
        holder.likeButton.setOnClickListener {
            if (currentUserId.isNullOrEmpty()) { // Check if user is logged in
                Toast.makeText(mContext, "You need to be logged in to like posts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (holder.likeButton.tag.toString() == "like") {
                FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
                    .child(currentUserId)
                    .setValue(true)
                pushNotification(postId, post.uid) // Direct access to post.uid
            } else {
                FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
                    .child(currentUserId)
                    .removeValue()
            }
        }

        // Listener for comments text and comment button (common action: go to AddCommentActivity or use lambda)
        val commentClickListener = View.OnClickListener {
            // If a lambda is provided in the constructor, use it
            onCommentClick?.invoke(post) ?: run {
                // Otherwise, fall back to the default AddCommentActivity intent
                val intent = Intent(mContext, AddCommentActivity::class.java).apply {
                    putExtra("POST_ID", postId)
                }
                mContext.startActivity(intent)
            }
        }
        holder.comments.setOnClickListener(commentClickListener)
        holder.commentButton.setOnClickListener(commentClickListener)


        // Listener for the likes count text (goes to ShowUsersActivity for likes)
        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id", postId)
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

        // Listener for the save button
        holder.saveButton.setOnClickListener {
            if (currentUserId.isNullOrEmpty()) { // Check if user is logged in
                Toast.makeText(mContext, "You need to be logged in to save posts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (holder.saveButton.tag == "Save") {
                FirebaseDatabase.getInstance().reference.child("Saves").child(currentUserId)
                    .child(postId).setValue(true)
                Toast.makeText(mContext, "Post Saved", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseDatabase.getInstance().reference.child("Saves").child(currentUserId)
                    .child(postId).removeValue()
                Toast.makeText(mContext, "Post Unsaved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var likes: TextView
        var comments: TextView
        var username: TextView
        var publisher: TextView
        var caption: TextView

        init {
            // Make sure these IDs match your posts_layout.xml
            profileImage = itemView.findViewById(R.id.publisher_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            likes = itemView.findViewById(R.id.likes)
            comments = itemView.findViewById(R.id.comments)
            username = itemView.findViewById(R.id.publisher_user_name_post)
            publisher = itemView.findViewById(R.id.publisher)
            caption = itemView.findViewById(R.id.caption)
        }
    }

    private fun getComments(postId: String, commentTextView: TextView) { // Renamed postid to postId for consistency
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comment").child(postId)

        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Database error in getComments: ${error.message}", error.toException())
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                commentTextView.text = "View all " + datasnapshot.childrenCount.toString() + " comments"
            }
        })
    }

    private fun pushNotification(postId: String, userId: String) { // Renamed postid to postId and userid to userId
        val currentUid = sessionManager.getUserId() // Get current user ID from SessionManager
        if (currentUid.isNullOrEmpty()) {
            Log.w("PostAdapter", "Current user is null or empty, cannot push notification.")
            return
        }

        // Push notification to the post's publisher (userId)
        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(userId)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userid"] = currentUid
        notifyMap["text"] = "♥liked your post♥"
        notifyMap["postid"] = postId
        notifyMap["ispost"] = true

        ref.push().setValue(notifyMap)
            .addOnFailureListener { e ->
                Log.e("PostAdapter", "Failed to push notification: ${e.message}", e)
            }
    }


    // Added currentUserId as a parameter to check against
    private fun isLiked(postId: String, imageView: ImageView, postedImg: ImageView, currentUserId: String?) {
        if (currentUserId.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.heart_not_clicked)
            imageView.tag = "like"
            postedImg.tag = "like"
            return
        }

        val postRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Database error in isLiked: ${error.message}", error.toException())
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                // Check if currentUserId exists under this post's likes
                if (datasnapshot.child(currentUserId).exists()) {
                    imageView.setImageResource(R.drawable.heart_clicked)
                    imageView.tag = "liked"
                    postedImg.tag = "liked"
                } else {
                    imageView.setImageResource(R.drawable.heart_not_clicked)
                    imageView.tag = "like"
                    postedImg.tag = "like"
                }
            }
        })
    }

    private fun getCountofLikes(postId: String, likesNo: TextView) { // Renamed postid to postId
        val postRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Database error in getCountofLikes: ${error.message}", error.toException())
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                likesNo.text = datasnapshot.childrenCount.toString() + " likes"
            }
        })
    }

    // Added currentUserId as a parameter to check against
    private fun isSaved(postId: String, imageView: ImageView, currentUserId: String?) { // Renamed postid to postId
        if (currentUserId.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.save_post_unfilled)
            imageView.tag = "Save"
            return
        }

        val savesRef = FirebaseDatabase.getInstance().reference.child("Saves").child(currentUserId)

        savesRef.addValueEventListener(object : ValueEventListener {
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
                Log.e("PostAdapter", "Database error in isSaved: ${error.message}", error.toException())
            }
        })
    }

    // Renamed publisherID to userId for clarity as it's a user's ID
    private fun publisherInfo(profileImage: CircleImageView, username: TextView, publisher: TextView, userId: String) {
        if (userId.isEmpty()) {
            Log.w("PostAdapter", "User ID is empty, cannot fetch user info.")
            Picasso.get().load(R.drawable.profile).into(profileImage)
            username.text = "Unknown User"
            publisher.text = "Unknown User"
            return
        }

        // IMPORTANT: Changed "Users" to "users" for consistency with UserProfileActivity
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Database error in publisherInfo: ${error.message}", error.toException())
                Picasso.get().load(R.drawable.profile).into(profileImage)
                username.text = "Error"
                publisher.text = "Error"
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let { nonNullUser ->
                        // Access properties directly (assuming User is a data class with 'image' and 'username')
                        Picasso.get().load(nonNullUser.image)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(profileImage)
                        username.text = nonNullUser.username
                        publisher.text = nonNullUser.username
                    } ?: run {
                        Log.w("PostAdapter", "User object is null for snapshot: ${snapshot.key}")
                        Picasso.get().load(R.drawable.profile).into(profileImage)
                        username.text = "User Not Found"
                        publisher.text = "User Not Found"
                    }
                } else {
                    Log.d("PostAdapter", "User data not found for UID: $userId")
                    Picasso.get().load(R.drawable.profile).into(profileImage)
                    username.text = "User Removed"
                    publisher.text = "User Removed"
                }
            }
        })
    }
}