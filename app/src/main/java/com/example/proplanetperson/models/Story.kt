package com.example.proplanetperson.models

import com.google.gson.annotations.SerializedName

data class Story(
    @SerializedName("storyId")
    val storyId: String = "",
    @SerializedName("userId") // Corresponds to Firebase's 'userid'
    val userId: String = "",
    @SerializedName("imageUrl") // Corresponds to Firebase's 'imageurl'
    val imageUrl: String = "",
    @SerializedName("timeStart") // Corresponds to Firebase's 'timestart'
    val timeStart: Long = 0L,
    @SerializedName("timeEnd") // Corresponds to Firebase's 'timeend'
    val timeEnd: Long = 0L
)