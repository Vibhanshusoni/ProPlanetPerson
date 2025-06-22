package com.example.proplanetperson.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.proplanetperson.models.YouTubeResponse // Replace with your actual response model

interface YouTubeApiService {

    @GET("search")
    fun searchVideos(
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 10,
        @Query("type") type: String = "video"
    ): Call<YouTubeResponse> // Replace with your actual data class
}
