package com.example.proplanetperson.data.remote

import com.example.proplanetperson.models.AuthResponse
import com.example.proplanetperson.models.UserAuthRequest
import retrofit2.Response // Import Response for wrapping network calls
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun registerUser(@Body request: UserAuthRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: UserAuthRequest): Response<AuthResponse>
}