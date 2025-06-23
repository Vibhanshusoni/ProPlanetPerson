package com.example.proplanetperson.models

import com.google.gson.annotations.SerializedName

data class Quote(
    @SerializedName("content")
    val content: String,
    @SerializedName("author")
    val author: String
    // You can add other fields if you need them, e.g.:
    // @SerializedName("_id")
    // val id: String,
    // @SerializedName("tags")
    // val tags: List<String>,
    // @SerializedName("length")
    // val length: Int
)