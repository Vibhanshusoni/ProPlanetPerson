package com.example.proplanetperson.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object YouTubeApiClient {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/youtube/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}