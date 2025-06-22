package com.example.proplanetperson.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // Base URL for the Quotable API
    private const val BASE_URL = "https://api.quotable.io/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // This is how you would get your QuoteApi service
    val api: QuoteApi by lazy {
        retrofit.create(QuoteApi::class.java)
    }
}
