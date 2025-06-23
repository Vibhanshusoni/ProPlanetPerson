package com.example.proplanetperson.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.R
import com.example.proplanetperson.fragments.ProfileFragment
import com.example.proplanetperson.models.User // Ensure this import is correct for your User data class
// Removed Firebase Auth imports
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.example.proplanetperson.utils.SessionManager // NEW: Import SessionManager
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import androidx.core.content.edit // For SharedPreferences.edit { } syntax

class UserAdapter(
    private val context: Context,
    private var userList: List<User>,
    private val isFragment: Boolean = false // This parameter is currently unused, consider removing if not needed.
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    // Removed FirebaseUser, now using SessionManager
    private lateinit var sessionManager: SessionManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Initialize SessionManager here, it's safe as Context is available
        sessionManager = SessionManager(context)

        val view = LayoutInflater.from(context).inflate(R.layout.user_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position] // The user displayed in this item
        val currentUserId = sessionManager.getUserId() // Get the current logged-in user's ID

        holder.userNameTextView.text = user.username
        holder.userFullnameTextView.text = user.fullname

        Picasso.get()
            .load(user.image)
            .placeholder(R.drawable.profile) // Make sure you have this drawable
            .error(R.drawable.profile) // Fallback in case of error loading image
            .into(holder.userProfileImage)

        // Pass the currentUserId to checkFollowingStatus
        checkFollowingStatus(user.uid, holder.followButton, currentUserId)

        // Click listener for the entire user item (to view profile)
        holder.userItem.setOnClickListener {
            // Using KTX extension for SharedPreferences.edit
            context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit {
                putString("profileid", user.uid)
            }

            if (context is FragmentActivity) {
                (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .addToBackStack(null) // Optional: Add to back stack
                    .commit()
            } else {
                Toast.makeText(context, "Cannot open profile (invalid context)", Toast.LENGTH_SHORT).show()
                Log.e("UserAdapter", "Context is not a FragmentActivity for profile navigation.")
            }
        }

        // Click listener for the Follow/Following button
        holder.followButton.setOnClickListener {
            // Ensure a user is logged in to perform follow actions
            if (currentUserId.isNullOrEmpty()) {
                Toast.makeText(context, "You need to be logged in to follow users.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit the listener if no user is logged in
            }

            if (holder.followButton.text.toString() == "Follow") {
                // Perform follow action
                FirebaseDatabase.getInstance().reference
                    .child("follow").child(currentUserId) // Consistent lowercase "follow"
                    .child("following").child(user.uid) // Consistent lowercase "following"
                    .setValue(true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            FirebaseDatabase.getInstance().reference
                                .child("follow").child(user.uid) // Consistent lowercase "follow"
                                .child("followers").child(currentUserId) // Consistent lowercase "followers"
                                .setValue(true)
                                .addOnCompleteListener { innerTask ->
                                    if (innerTask.isSuccessful) {
                                        pushNotification(user.uid) // Push notification to the user being followed
                                    } else {
                                        Log.e("UserAdapter", "Failed to add follower: ${innerTask.exception?.message}")
                                    }
                                }
                        } else {
                            Log.e("UserAdapter", "Failed to add following: ${task.exception?.message}")
                        }
                    }
            } else {
                // Perform unfollow action
                FirebaseDatabase.getInstance().reference
                    .child("follow").child(currentUserId) // Consistent lowercase "follow"
                    .child("following").child(user.uid) // Consistent lowercase "following"
                    .removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            FirebaseDatabase.getInstance().reference
                                .child("follow").child(user.uid) // Consistent lowercase "follow"
                                .child("followers").child(currentUserId) // Consistent lowercase "followers"
                                .removeValue()
                        } else {
                            Log.e("UserAdapter", "Failed to remove following: ${task.exception?.message}")
                        }
                    }
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.user_item_search_username)
        val userFullnameTextView: TextView = itemView.findViewById(R.id.user_item_search_fullname)
        val userProfileImage: CircleImageView = itemView.findViewById(R.id.user_item_image)
        val followButton: Button = itemView.findViewById(R.id.user_item_follow_button)
        val userItem: LinearLayout = itemView.findViewById(R.id.user_item_container) // The whole clickable item
    }

    // Added currentUserId as a parameter, and made it nullable as sessionManager.getUserId() returns String?
    private fun checkFollowingStatus(uidToCheck: String, followButton: Button, currentUserId: String?) {
        // If no user is logged in, the button should display "Follow" (as they can't be following)
        if (currentUserId.isNullOrEmpty()) {
            followButton.text = "Follow"
            return
        }

        // Don't show follow button for the current user's own profile (if this adapter is used for displaying self)
        if (uidToCheck == currentUserId) {
            followButton.visibility = View.GONE
            return
        } else {
            followButton.visibility = View.VISIBLE
        }

        // Consistent lowercase "follow" and "following"
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("follow").child(currentUserId).child("following")

        followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if the current user is following the user with uidToCheck
                followButton.text = if (snapshot.hasChild(uidToCheck)) "Following" else "Follow"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserAdapter", "Error checking follow status: ${error.message}", error.toException())
            }
        })
    }

    // Pushes a notification to the userId indicating that currentUserId started following them
    private fun pushNotification(userId: String) {
        val currentUserId = sessionManager.getUserId() // Get current user ID from SessionManager
        if (currentUserId.isNullOrEmpty()) {
            Log.w("UserAdapter", "Current user is null or empty, cannot push notification.")
            return
        }

        // Consistent lowercase "notification"
        val ref = FirebaseDatabase.getInstance().reference
            .child("notification").child(userId)

        val notificationMap = hashMapOf<String, Any>(
            "userid" to currentUserId,
            "text" to "started following you",
            "postid" to "", // No post associated with this type of notification
            "ispost" to false // Indicates this is not a post-related notification
        )

        ref.push().setValue(notificationMap).addOnFailureListener { e ->
            Log.e("UserAdapter", "Notification push failed: ${e.message}", e)
        }
    }
}