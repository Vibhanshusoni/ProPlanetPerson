package com.example.proplanetperson.api

import com.example.proplanetperson.models.Quote
import retrofit2.Call
import retrofit2.http.GET

// This interface should be part of your existing API setup, e.g., in a file like QuoteApi.kt
// or combined with other API calls if you have a single interface.
interface QuoteApi {
    @GET("random") // This is the endpoint for a random quote
    fun getRandomQuote(): Call<Quote> // Returns a single Quote object
}
