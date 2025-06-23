package com.example.proplanetperson.fragments

import android.os.Bundle
import android.util.Log // Import Log for better error logging
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // Import Toast for user feedback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.R
import com.example.proplanetperson.adapters.NotificationAdapter
import com.example.proplanetperson.models.Notification
// Removed Firebase Auth imports
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.proplanetperson.utils.SessionManager // NEW: Import SessionManager
import java.util.*
import kotlin.collections.ArrayList

class NotificationFragment : Fragment() {

    private var notificationAdapter: NotificationAdapter? = null
    // Changed to be non-nullable and initialized immediately for safety
    private var notificationList: MutableList<Notification> = ArrayList()
    // Removed firebaseUser, using SessionManager now
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        // Initialize SessionManager with requireContext()
        sessionManager = SessionManager(requireContext())

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerview_notification)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        // Initialize adapter using requireContext() and the non-nullable notificationList
        notificationAdapter = NotificationAdapter(requireContext(), notificationList as ArrayList<Notification>)
        recyclerView.adapter = notificationAdapter

        readNotification() // Call to read notifications
        return view
    }

    private fun readNotification() {
        val currentUserId = sessionManager.getUserId() // Get current user ID from SessionManager

        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please log in to view notifications.", Toast.LENGTH_SHORT).show()
            Log.w("NotificationFragment", "User not logged in, cannot read notifications.")
            return
        }

        // IMPORTANT: Changed "Notification" to "notification" for consistency
        val postRef = FirebaseDatabase.getInstance().reference.child("notification").child(currentUserId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                // Log the error and show a user-friendly message
                Log.e("NotificationFragment", "Failed to read notifications: ${error.message}", error.toException())
                Toast.makeText(requireContext(), "Error loading notifications.", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                notificationList.clear() // Clear the list directly, no need for ?.
                for (snapshot in p0.children) {
                    val notification: Notification? = snapshot.getValue(Notification::class.java)
                    // Safely add notification only if it's not null
                    notification?.let {
                        notificationList.add(it)
                    }
                }
                // Reverse the list to show newest first (optional, based on your preference)
                Collections.reverse(notificationList)
                notificationAdapter?.notifyDataSetChanged() // Safely call notifyDataSetChanged()
            }
        })
    }
}