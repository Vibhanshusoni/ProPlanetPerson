
package com.example.proplanetperson.api

import com.example.proplanetperson.models.AuthResponse
import com.example.proplanetperson.models.User
import com.example.proplanetperson.models.UserAuthRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi : ApiService { // Make sure ApiService has these functions declared for 'override' to be valid

    // --- CHANGE THIS LINE ---
    @POST("api/auth/register") // <--- ADDED '/auth' HERE
    override suspend fun registerUser(@Body request: UserAuthRequest): Response<AuthResponse>

    // --- CHANGE THIS LINE ---
    @POST("api/auth/login") // <--- ADDED '/auth' HERE
    override suspend fun loginUser(@Body user: User): Response<AuthResponse>

    // --- CHANGE THIS LINE ---
    @POST("api/auth/google-login") // <--- ADDED '/auth' HERE
    override suspend fun googleLogin(@Body body: Map<String, String>): Response<AuthResponse>
}