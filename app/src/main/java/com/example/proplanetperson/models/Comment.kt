package com.example.proplanetperson.models
data class Comment(
    val commentText: String = "", // Make sure this matches your Firebase node
    val publisherId: String = "", // Make sure this matches your Firebase node
    val commentId: String = "" // You might have a comment ID too
)