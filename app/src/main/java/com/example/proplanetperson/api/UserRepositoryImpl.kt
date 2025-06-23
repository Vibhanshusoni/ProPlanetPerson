package com.example.proplanetperson.api

import com.example.proplanetperson.models.LoginRequest
import com.example.proplanetperson.models.LoginResponse
import com.example.proplanetperson.models.ServerStatus
import com.example.proplanetperson.models.User
import com.example.proplanetperson.utils.Resource
import retrofit2.HttpException
import java.io.IOException

class UserRepositoryImpl(private val userApi: UserApi) : UserRepository {

    override suspend fun registerUser(user: User): Resource<User> {
        return try {
            val response = userApi.registerUser(user)
            if (response.isSuccessful) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Registration failed")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error during registration")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred during registration")
        }
    }

    override suspend fun loginUser(request: LoginRequest): Resource<LoginResponse> {
        return try {
            val response = userApi.loginUser(request)
            if (response.isSuccessful) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Login failed")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error during login")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred during login")
        }
    }

    override suspend fun getUserProfile(userId: String, authToken: String): Resource<User> {
        return try {
            val response = userApi.getUserProfile(userId, "Bearer $authToken")
            if (response.isSuccessful) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to fetch user profile")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error fetching user profile")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred while fetching user profile")
        }
    }

    override suspend fun getServerStatus(): Resource<ServerStatus> {
        return try {
            val response = userApi.getServerStatus()
            if (response.isSuccessful) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to get server status")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error getting server status")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred getting server status")
        }
    }

    // --- NEW: Implement updateUserProfile method ---
    override suspend fun updateUserProfile(userId: String, authToken: String, updatedUser: User): Resource<User> {
        return try {
            // Assuming your UserApi has an endpoint for updating user profiles.
            // Example:
            // suspend fun updateUserProfile(@Path("userId") userId: String, @Header("Authorization") authToken: String, @Body updatedUser: User): Response<User>
            val response = userApi.updateUserProfile(userId, "Bearer $authToken", updatedUser)
            if (response.isSuccessful) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to update user profile")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error updating user profile")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred while updating user profile")
        }
    }
    // --- END NEW ---
}