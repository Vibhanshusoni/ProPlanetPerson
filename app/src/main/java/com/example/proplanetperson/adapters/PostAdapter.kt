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
import com.example.proplanetperson.models.User
import com.example.proplanetperson.models.Post // Ensure this import is correct for your Post data class
import com.example.proplanetperson.R
import com.example.proplanetperson.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
// Import the KTX extension function for SharedPreferences.edit
import androidx.core.content.edit // This import is crucial for the SharedPreferences.edit { } syntax

// Removed the manually defined SharedPreferences.edit extension function to avoid conflicts.
// It's provided by 'androidx.core:core-ktx' which you already have.

// Added onCommentClick lambda to the constructor to handle comment button clicks from HomeFragment
class PostAdapter(
    private val mContext: Context,
    private val mPost: List<Post>,
    private val onCommentClick: ((Post) -> Unit)? = null // Nullable lambda for optional handling
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    // code for events
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]
        // Access properties directly, handling potential nulls
        // IMPORTANT: Ensure your Post data class has properties named 'postid', 'postimage', 'caption', 'publisher'
        // If your Post data class uses different names (e.g., 'postId', 'imageUrl'), you MUST change them here
        // or rename the properties in your Post data class to match your Firebase database keys.
        val postid = post.postId ?: "" // Default to empty string if null

        Picasso.get().load(post.postimage) // Direct access
            .placeholder(R.drawable.ic_profile_placeholder) // Make sure you have this drawable
            .error(R.drawable.error_image) // Make sure you have this drawable
            .into(holder.postImage)

        holder.caption.text = post.caption // Direct access
        publisherInfo(holder.profileImage, holder.username, holder.publisher, post.publisher ?: "") // Direct access, handle null
        isLiked(postid, holder.likeButton, holder.postImage) // Direct access
        isSaved(postid, holder.saveButton) // Direct access
        getCountofLikes(postid, holder.likes) // Direct access
        getComments(postid, holder.comments) // Direct access

        // --- Consolidated and Corrected Click Listeners ---

        // Listener for publisher text and profile image (common action: view publisher's profile)
        val publisherClickListener = View.OnClickListener {
            val publisherId = post.publisher ?: "" // Direct access, handle null
            if (publisherId.isNotEmpty()) {
                // Using KTX extension for SharedPreferences.edit
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                    putString("profileid", publisherId)
                }

                if (mContext is FragmentActivity) {
                    mContext.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .addToBackStack(null) // Optional: Add to back stack
                        .commit()
                } else {
                    Log.e("PostAdapter", "Context is not a FragmentActivity for profile navigation.")
                }
            } else {
                Log.w("PostAdapter", "Publisher ID is empty for profile click.")
            }
        }

        holder.publisher.setOnClickListener(publisherClickListener)
        holder.profileImage.setOnClickListener(publisherClickListener)
        holder.username.setOnClickListener(publisherClickListener) // username also goes to profile


        // Listener for post image (different actions: view post details on single tap)
        holder.postImage.setOnClickListener {
            // Using KTX extension for SharedPreferences.edit
            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                putString("postid", postid) // Use the 'postid' variable
            }

            if (mContext is FragmentActivity) {
                mContext.supportFragmentManager
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
            if (firebaseUser == null) {
                Toast.makeText(mContext, "You need to be logged in to like posts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (holder.likeButton.tag.toString() == "like") {
                FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
                    .child(firebaseUser!!.uid)
                    .setValue(true)
                pushNotification(postid, post.publisher ?: "") // Direct access, handle null
            } else {
                FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
                    .child(firebaseUser!!.uid)
                    .removeValue()
            }
        }

        // Listener for comments text and comment button (common action: go to AddCommentActivity or use lambda)
        val commentClickListener = View.OnClickListener {
            // If a lambda is provided in the constructor, use it
            onCommentClick?.invoke(post) ?: run {
                // Otherwise, fall back to the default AddCommentActivity intent
                val intent = Intent(mContext, AddCommentActivity::class.java).apply {
                    putExtra("POST_ID", postid)
                }
                mContext.startActivity(intent)
            }
        }
        holder.comments.setOnClickListener(commentClickListener)
        holder.commentButton.setOnClickListener(commentClickListener)


        // Listener for the likes count text (goes to ShowUsersActivity for likes)
        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id", postid)
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

        // Listener for the save button
        holder.saveButton.setOnClickListener {
            if (firebaseUser == null) {
                Toast.makeText(mContext, "You need to be logged in to save posts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (holder.saveButton.tag == "Save") {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
                    .child(postid).setValue(true)
                Toast.makeText(mContext, "Post Saved", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
                    .child(postid).removeValue()
                Toast.makeText(mContext, "Post Unsaved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Removed @NonNull from ViewHolder constructor
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

    private fun getComments(postid: String, commentTextView: TextView) {
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comment").child(postid)

        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Database error in getComments: ${error.message}", error.toException())
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                commentTextView.text = "View all " + datasnapshot.childrenCount.toString() + " comments"
            }
        })
    }

    private fun pushNotification(postid: String, userid: String) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUid == null) {
            Log.w("PostAdapter", "Current user is null, cannot push notification.")
            return
        }

        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(userid)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userid"] = currentUid
        notifyMap["text"] = "♥liked your post♥"
        notifyMap["postid"] = postid
        notifyMap["ispost"] = true

        ref.push().setValue(notifyMap)
            .addOnFailureListener { e ->
                Log.e("PostAdapter", "Failed to push notification: ${e.message}", e)
            }
    }


    private fun isLiked(postid: String, imageView: ImageView, postedImg: ImageView) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            imageView.setImageResource(R.drawable.heart_not_clicked)
            imageView.tag = "like"
            postedImg.tag = "like"
            return
        }

        val postRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Database error in isLiked: ${error.message}", error.toException())
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                if (datasnapshot.child(firebaseUser!!.uid).exists()) {
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

    private fun getCountofLikes(postid: String, likesNo: TextView) {
        val postRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("PostAdapter", "Database error in getCountofLikes: ${error.message}", error.toException())
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                likesNo.text = datasnapshot.childrenCount.toString() + " likes"
            }
        })
    }

    private fun isSaved(postid: String, imageView: ImageView) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            imageView.setImageResource(R.drawable.save_post_unfilled)
            imageView.tag = "Save"
            return
        }

        val savesRef = FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)

        savesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(postid).exists()) {
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

    private fun publisherInfo(profileImage: CircleImageView, username: TextView, publisher: TextView, publisherID: String) {
        if (publisherID.isEmpty()) {
            Log.w("PostAdapter", "Publisher ID is empty, cannot fetch user info.")
            Picasso.get().load(R.drawable.profile).into(profileImage)
            username.text = "Unknown User"
            publisher.text = "Unknown User"
            return
        }

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
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
                    Log.d("PostAdapter", "User data not found for UID: $publisherID")
                    Picasso.get().load(R.drawable.profile).into(profileImage)
                    username.text = "User Removed"
                    publisher.text = "User Removed"
                }
            }
        })
    }
}
