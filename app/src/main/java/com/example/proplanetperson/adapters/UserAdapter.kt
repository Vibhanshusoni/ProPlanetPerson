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
import com.example.proplanetperson.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private val context: Context,
    private var userList: List<User>,
    private val isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.userNameTextView.text = user.username
        holder.userFullnameTextView.text = user.fullname

        Picasso.get()
            .load(user.image)
            .placeholder(R.drawable.profile)
            .into(holder.userProfileImage)

        checkFollowingStatus(user.uid, holder.followButton)

        holder.userItem.setOnClickListener {
            val prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            prefs.putString("profileid", user.uid)
            prefs.apply()

            if (context is FragmentActivity) {
                (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .commit()
            } else {
                Toast.makeText(context, "Cannot open profile (invalid context)", Toast.LENGTH_SHORT).show()
            }
        }

        holder.followButton.setOnClickListener {
            val currentUserId = firebaseUser?.uid ?: return@setOnClickListener

            if (holder.followButton.text.toString() == "Follow") {
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(currentUserId)
                    .child("Following").child(user.uid)
                    .setValue(true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            FirebaseDatabase.getInstance().reference
                                .child("Follow").child(user.uid)
                                .child("Followers").child(currentUserId)
                                .setValue(true)
                                .addOnCompleteListener {
                                    pushNotification(user.uid)
                                }
                        }
                    }
            } else {
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(currentUserId)
                    .child("Following").child(user.uid)
                    .removeValue()
                    .addOnCompleteListener {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(user.uid)
                            .child("Followers").child(currentUserId)
                            .removeValue()
                    }
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.user_item_search_username)
        val userFullnameTextView: TextView = itemView.findViewById(R.id.user_item_search_fullname)
        val userProfileImage: CircleImageView = itemView.findViewById(R.id.user_item_image)
        val followButton: Button = itemView.findViewById(R.id.user_item_follow_button)
        val userItem: LinearLayout = itemView.findViewById(R.id.user_item_container)
    }

    private fun checkFollowingStatus(uid: String, followButton: Button) {
        val currentUserId = firebaseUser?.uid ?: return

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(currentUserId).child("Following")

        followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followButton.text = if (snapshot.hasChild(uid)) "Following" else "Follow"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserAdapter", "Error checking follow status: ${error.message}")
            }
        })
    }

    private fun pushNotification(userId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val ref = FirebaseDatabase.getInstance().reference
            .child("Notification").child(userId)

        val notificationMap = hashMapOf<String, Any>(
            "userid" to currentUserId,
            "text" to "started following you",
            "postid" to "",
            "ispost" to false
        )

        ref.push().setValue(notificationMap).addOnFailureListener { e ->
            Log.e("UserAdapter", "Notification push failed: ${e.message}", e)
        }
    }
}
