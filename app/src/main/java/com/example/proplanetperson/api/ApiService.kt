// com.example.proplanetperson.api.ApiService.kt
package com.example.proplanetperson.api

import com.example.proplanetperson.models.AuthResponse
import com.example.proplanetperson.models.User
import com.example.proplanetperson.models.UserAuthRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/register") // Adjust endpoint as per your backend
    suspend fun registerUser(@Body request: UserAuthRequest): Response<AuthResponse>

    @POST("api/login") // Adjust endpoint as per your backend
    suspend fun loginUser(@Body user: User): Response<AuthResponse>

    @POST("api/google-login") // Adjust endpoint as per your backend
    suspend fun googleLogin(@Body idToken: Map<String, String>): Response<AuthResponse>
}