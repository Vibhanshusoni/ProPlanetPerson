package com.example.proplanetperson.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("token")
    val token: String?,
    @SerializedName("userId") // Your backend should return the user ID
    val userId: String?
)