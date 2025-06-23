package com.example.proplanetperson.api

import com.example.proplanetperson.models.AuthResponse
import com.example.proplanetperson.models.UserAuthRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/register") // <--- REPLACE WITH YOUR ACTUAL REGISTER ENDPOINT
    suspend fun registerUser(@Body request: UserAuthRequest): Response<AuthResponse>

    @POST("api/login") // <--- REPLACE WITH YOUR ACTUAL LOGIN ENDPOINT
    suspend fun loginUser(@Body request: UserAuthRequest): Response<AuthResponse>

    @POST("api/google-login") // <--- REPLACE WITH YOUR ACTUAL GOOGLE LOGIN ENDPOINT
    suspend fun googleLogin(@Body idToken: String): Response<AuthResponse> // Assuming backend expects plain ID token
    // If your backend expects a JSON object like { "idToken": "..." }, you might need:
    // suspend fun googleLogin(@Body body: Map<String, String>): Response<AuthResponse>
}