package com.example.proplanetperson

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.adapters.UserAdapter
import com.example.proplanetperson.models.User
import com.google.firebase.database.*

class ShowUsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = ArrayList<User>()

    private val idList = ArrayList<String>()
    private lateinit var usersRef: DatabaseReference
    private lateinit var followRef: DatabaseReference
    private lateinit var likesRef: DatabaseReference

    private lateinit var textNoUsers: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        val id = intent.getStringExtra("id") ?: ""
        val title = intent.getStringExtra("title") ?: ""

        supportActionBar?.title = title

        recyclerView = findViewById(R.id.recycler_view_users)
        textNoUsers = findViewById(R.id.text_no_users)
        progressBar = findViewById(R.id.progress_bar_users)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        userAdapter = UserAdapter(this, userList)
        recyclerView.adapter = userAdapter

        when (title.lowercase()) {
            "followers" -> getFollowers(id)
            "following" -> getFollowing(id)
            "likes" -> getLikes(id)
        }
    }

    private fun getFollowers(id: String) {
        progressBar.visibility = View.VISIBLE
        followRef = FirebaseDatabase.getInstance().getReference("Follow").child(id).child("followers")
        followRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                idList.clear()
                for (data in snapshot.children) {
                    data.key?.let { idList.add(it) }
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun getFollowing(id: String) {
        progressBar.visibility = View.VISIBLE
        followRef = FirebaseDatabase.getInstance().getReference("Follow").child(id).child("following")
        followRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                idList.clear()
                for (data in snapshot.children) {
                    data.key?.let { idList.add(it) }
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun getLikes(postId: String) {
        progressBar.visibility = View.VISIBLE
        likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(postId)
        likesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                idList.clear()
                for (data in snapshot.children) {
                    data.key?.let { idList.add(it) }
                }
                showUsers()
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun showUsers() {
        usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    if (user != null && idList.contains(user.uid)) {
                        userList.add(user)
                    }
                }

                progressBar.visibility = View.GONE
                textNoUsers.visibility = if (userList.isEmpty()) View.VISIBLE else View.GONE
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
            }
        })
    }
}
