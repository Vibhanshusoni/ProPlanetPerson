package com.example.proplanetperson.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") // or "email" depending on your backend
    val username: String,
    @SerializedName("password")
    val password: String
)