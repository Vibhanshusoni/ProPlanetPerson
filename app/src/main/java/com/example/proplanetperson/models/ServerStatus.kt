package com.example.proplanetperson.models

import com.google.gson.annotations.SerializedName

data class ServerStatus(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String
)