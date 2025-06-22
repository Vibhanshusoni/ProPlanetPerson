package com.example.proplanetperson.models

data class Post(
    val postId: String? = null,
    val userId: String? = null,
    val postimage: String? = null, // Make sure this matches your Firebase key
    val caption: String? = null,   // Make sure this matches your Firebase key
    val publisher: String? = null, // Make sure this matches your Firebase key
    val imageUrl: String? = null, // This is the property Picasso needs
    val description: String? = null,
    val timestamp: Long? = null
)