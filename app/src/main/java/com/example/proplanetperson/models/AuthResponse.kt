package com.example.proplanetperson.models

data class AuthResponse(
    val userId: String,
    val token: String,
    val message: String? = null,
    val user: User? = null // Optional: Include the full User object if your backend returns it
)

// You will also need your User data class, as used in AccountSettings and UserAuthRequest:
/*
data class User(
    val userId: String, // Assuming your user model also has a userId
    val fullname: String,
    val username: String,
    val bio: String,
    val image: String
    // Add other user profile fields as needed
)
*/