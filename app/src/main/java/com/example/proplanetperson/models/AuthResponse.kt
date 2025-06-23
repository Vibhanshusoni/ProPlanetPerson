package com.example.proplanetperson.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_role")
    val userRole: String
)