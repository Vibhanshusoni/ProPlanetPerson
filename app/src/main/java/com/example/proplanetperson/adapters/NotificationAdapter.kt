package com.example.proplanetperson.adapters

import android.content.Context
import android.widget.Toast
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull // Required for @NonNull annotation on ViewHolder constructor
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.R
import com.example.proplanetperson.fragments.ProfileFragment // Assuming you navigate to profile
import com.example.proplanetperson.models.Notification
import com.example.proplanetperson.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(
    private val mContext: Context,
    private val mNotification: List<Notification> // List of your Notification model
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notification_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNotification.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotification[position]

        // Populate notification details (direct property access)
        holder.text.text = notification.text

        // Fetch user information (e.g., profile image and username of the one who triggered the notification)
        getUserInfo(holder.profileImage, holder.userName, notification.userid)

        // Handle clicks on the notification item
        holder.itemView.setOnClickListener {
            // Depending on 'ispost', navigate to post detail or user profile
            if (notification.ispost) {
                // Example: Navigate to PostDetailsActivity (You'll need to create this activity)
                // If you have a PostDetailsActivity to show a single post:
                // val intent = Intent(mContext, PostDetailsActivity::class.java)
                // intent.putExtra("postid", notification.postid)
                // mContext.startActivity(intent)
                Log.d("NotificationAdapter", "Navigating to post details for post ID: ${notification.postid}")
                // As a fallback, for now, let's just log or show a toast
                Toast.makeText(mContext, "Post details for ${notification.postid} not yet implemented.", Toast.LENGTH_SHORT).show()

            } else {
                // Example: Navigate to ProfileFragment or ProfileActivity
                val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("profileid", notification.userid)
                pref.apply()

                if (mContext is FragmentActivity) {
                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
                } else {
                    Log.e("NotificationAdapter", "Context is not a FragmentActivity, cannot open user profile.")
                    Toast.makeText(mContext, "Error: Cannot open user profile. Invalid context.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Optionally, handle post image if 'ispost' is true
        if (notification.ispost && notification.postid.isNotEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage, notification.postid)
        } else {
            holder.postImage.visibility = View.GONE
        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView = itemView.findViewById(R.id.notification_profile_image)
        var postImage: ImageView = itemView.findViewById(R.id.notification_post_image)
        var userName: TextView = itemView.findViewById(R.id.notification_username)
        var text: TextView = itemView.findViewById(R.id.notification_text)
    }

    private fun getUserInfo(profileImage: CircleImageView, userName: TextView, publisherId: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        Picasso.get().load(it.image).placeholder(R.drawable.profile).into(profileImage)
                        userName.text = it.username
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationAdapter", "Failed to load user info for notification: ${error.message}", error.toException())
            }
        })
    }

    private fun getPostImage(postImage: ImageView, postId: String) {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val postImageUrl = snapshot.child("postimage").getValue(String::class.java) // Assuming 'postimage' is the key for the image URL in your Post node
                    postImageUrl?.let {
                        Picasso.get().load(it).placeholder(R.drawable.ic_profile_placeholder).into(postImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationAdapter", "Failed to load post image for notification: ${error.message}", error.toException())
            }
        })
    }
}