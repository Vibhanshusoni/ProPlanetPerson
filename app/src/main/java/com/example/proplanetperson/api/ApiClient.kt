package com.example.proplanetperson.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // --- BASE URLs ---
    // IMPORTANT: REPLACE WITH YOUR ACTUAL BACKEND BASE URL
    private const val BACKEND_BASE_URL = "http://10.0.2.2:5000/"

    // IMPORTANT: Use the correct base URL for your external Quote API
    private const val QUOTE_API_BASE_URL = "https://api.quotable.io/" // Example: for Quotable API

    // --- OkHttpClient with Logging and Timeouts ---
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies for debugging
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
        .build()

    // --- Retrofit Instance for your Custom Backend API ---
    val backendRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(okHttpClient) // Use the configured OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- Retrofit Instance for the External Quote API ---
    // It uses a different base URL but can share the same OkHttpClient
    val quoteRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(QUOTE_API_BASE_URL)
            .client(okHttpClient) // Use the configured OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- API Service Interfaces ---
    // Services for your custom backend
    val postApi: PostApi by lazy {
        backendRetrofit.create(PostApi::class.java)
    }

    val userApi: UserApi by lazy {
        backendRetrofit.create(UserApi::class.java)
    }

    val authApi: AuthApi by lazy {
        backendRetrofit.create(AuthApi::class.java)
    }

    // Service for the external Quote API
    val quoteApi: QuoteApi by lazy {
        quoteRetrofit.create(QuoteApi::class.java) // Create QuoteApi using the dedicated quoteRetrofit instance
    }

    // You can add other API services here as needed, creating new Retrofit instances
    // if they have different base URLs, or using backendRetrofit if they share the backend URL.
}