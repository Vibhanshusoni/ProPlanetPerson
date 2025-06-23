package com.example.proplanetperson.api

import com.example.proplanetperson.models.LoginRequest
import com.example.proplanetperson.models.LoginResponse
import com.example.proplanetperson.models.ServerStatus
import com.example.proplanetperson.models.User
import com.example.proplanetperson.utils.Resource // Ensure this is imported

interface UserRepository {
    suspend fun registerUser(user: User): Resource<User>
    suspend fun loginUser(request: LoginRequest): Resource<LoginResponse>
    suspend fun getUserProfile(userId: String, authToken: String): Resource<User>
    suspend fun getServerStatus(): Resource<ServerStatus>

    // --- NEW: Add updateUserProfile to the interface ---
    // Assuming you'll have an endpoint to update user profile
    // This signature is a common example; adjust parameters as per your backend API for updating a user.
    // For example, you might send a User object with updated fields.
    suspend fun updateUserProfile(userId: String, authToken: String, updatedUser: User): Resource<User>
    // --- END NEW ---
}