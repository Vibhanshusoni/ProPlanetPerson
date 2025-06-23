package com.example.proplanetperson.models

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("postId") // Matches Firebase key and property name
    val postId: String = "",

    @SerializedName("imageUrl") // Matches Firebase key and property name
    val imageUrl: String = "", // RENAMED from 'postimage' for consistency with MediaUploadActivity and MyPostAdapter

    @SerializedName("caption") // Matches Firebase key and property name
    val caption: String = "",

    @SerializedName("uid") // IMPORTANT: Changed from "publisherId" to "uid" to match MediaUploadActivity and UserProfileActivity queries
    val uid: String = "", // RENAMED from 'publisher' to 'uid' for consistency

    @SerializedName("timestamp") // Matches Firebase key and property name
    val timestamp: Long = 0L,

    @SerializedName("type") // Added 'type' as it's saved in MediaUploadActivity
    val type: String = "" // e.g., "photo", "video"
)