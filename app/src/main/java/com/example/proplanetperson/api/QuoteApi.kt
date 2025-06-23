package com.example.proplanetperson.api

import com.example.proplanetperson.models.Quote
import retrofit2.Response // Use Retrofit's Response class
import retrofit2.http.GET

interface QuoteApi {
    @GET("quotes/random") // <--- CONFIRM YOUR ACTUAL QUOTE API ENDPOINT
    suspend fun getRandomQuote(): Response<Quote>
}