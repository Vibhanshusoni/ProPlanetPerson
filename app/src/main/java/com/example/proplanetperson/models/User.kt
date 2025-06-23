// com.example.proplanetperson.models.User.kt
package com.example.proplanetperson.models

// Ensure 'email' and 'password' are correctly defined as constructor parameters
data class User(
    val email: String,
    val password: String,
    // Add other properties if your User model contains them
    val uid: String = "",
    val userId: String? = null, // Example: for returned user data
    val username: String? = null,
    val fullname: String? = null,
    val bio: String? = null,
    val image: String? = null
)