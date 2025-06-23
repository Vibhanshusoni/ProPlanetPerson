package com.example.proplanetperson.models
data class User(
    val uid: String = "",
    val username: String = "",
    val fullname: String = "",
    val email: String = "",
    val bio: String = "",
    val image: String = "" // This is the profile image URL
)