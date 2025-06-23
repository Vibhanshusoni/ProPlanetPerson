package com.example.proplanetperson.api

import com.example.proplanetperson.models.LoginRequest
import com.example.proplanetperson.models.LoginResponse
import com.example.proplanetperson.models.ServerStatus
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/api/status")
    fun getServerStatus(): Call<ServerStatus>

    @POST("/api/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>
}