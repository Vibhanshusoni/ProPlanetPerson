package com.example.proplanetperson.api

import com.example.proplanetperson.models.LoginRequest
import com.example.proplanetperson.models.LoginResponse
import com.example.proplanetperson.models.ServerStatus // Ensure ServerStatus is imported
import com.example.proplanetperson.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    @POST("api/register") // Example endpoint, adjust as per your backend
    suspend fun registerUser(@Body user: User): Response<User> // Assuming your User model is complete

    @POST("api/login") // Example endpoint, adjust as per your backend
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/profile/{userId}") // Example endpoint, adjust as per your backend
    suspend fun getUserProfile(
        @Path("userId") userId: String,
        @Header("Authorization") authToken: String
    ): Response<User>

    // --- NEW: For server status check ---
    @GET("api/status") // <--- YOUR ACTUAL STATUS ENDPOINT
    suspend fun getServerStatus(): Response<ServerStatus>
    // --- END NEW ---
    // In UserApi.kt
        // ... (existing methods) ...

    @PUT("api/profile/{userId}") // Or @POST, @PATCH depending on your backend
    suspend fun updateUserProfile(
      @Path("userId") userId: String,
      @Header("Authorization") authToken: String,
      @Body updatedUser: User
    ): Response<User>
}