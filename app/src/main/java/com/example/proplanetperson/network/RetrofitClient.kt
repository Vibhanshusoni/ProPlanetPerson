package com.example.proplanetperson.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.proplanetperson.data.remote.AuthApiService
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Your backend server URL
    // If running on an AVD (emulator), use 10.0.2.2 instead of localhost
    // If running on a physical device, use your machine's local IP address (e.g., 192.168.1.X)
    private const val BASE_URL = "http://10.0.2.2:5000/" // <-- IMPORTANT: Change this based on your setup!

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Add logging for debugging
        .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
        .build()

    val authApiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}