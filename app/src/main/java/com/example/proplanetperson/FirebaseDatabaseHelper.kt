package com.example.proplanetperson

import com.example.proplanetperson.models.User
import com.example.proplanetperson.models.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError

import kotlin.jvm.java

object FirebaseDatabaseHelper {
    private val db = FirebaseDatabase.getInstance()

    fun uploadUser(uid: String, user: User) {
        db.getReference("users").child(uid).setValue(user)
    }

    fun uploadPost(postId: String, post: Post, userId: String) {
        db.getReference("posts").child(postId).setValue(post)
        db.getReference("user_posts").child(userId).child(postId).setValue(true)
    }

    fun getUser(uid: String, onResult: (User?) -> Unit) {
        db.getReference("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.getValue(User::class.java))
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult(null)
                }
            })
    }
}
