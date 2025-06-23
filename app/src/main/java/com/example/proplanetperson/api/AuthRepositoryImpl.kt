package com.example.proplanetperson.api

import android.util.Log
import com.example.proplanetperson.models.AuthResponse
import com.example.proplanetperson.models.User
import com.example.proplanetperson.models.UserAuthRequest
import com.example.proplanetperson.utils.Resource
import retrofit2.HttpException
import java.io.IOException

// Assuming this is your implementation of AuthRepository (if you have an interface)
// Or if you only have one repository class, you might name it AuthRepository.
class AuthRepositoryImpl(private val apiService: ApiService) { // Injects ApiService

    suspend fun registerUser(request: UserAuthRequest): Resource<AuthResponse> {
        return try {
            Log.d("AuthRepository", "Attempting registration for email: ${request.email}")
            val response = apiService.registerUser(request)
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    Log.d("AuthRepository", "Registration Success: ${authResponse.message}")
                    Resource.Success(authResponse)
                } else {
                    Log.e("AuthRepository", "Registration Error: Empty response body")
                    Resource.Error("Empty response body from server.")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Registration Error: ${response.code()} - $errorBody")
                Resource.Error(errorBody ?: "Registration failed with code ${response.code()}")
            }
        } catch (e: HttpException) {
            Log.e("AuthRepository", "Registration HTTP Exception: ${e.message()}", e)
            Resource.Error(e.message() ?: "HTTP error during registration")
        } catch (e: IOException) {
            Log.e("AuthRepository", "Registration Network Error: ${e.message}", e)
            Resource.Error("Network error: Check your internet connection.")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration Generic Exception: ${e.message}", e)
            Resource.Error("An unexpected error occurred during registration.")
        }
    }

    suspend fun loginUser(user: User): Resource<AuthResponse> {
        return try {
            Log.d("AuthRepository", "Attempting login for email: ${user.email}")
            val response = apiService.loginUser(user)
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    Log.d("AuthRepository", "Login Success: ${authResponse.message}")
                    Resource.Success(authResponse)
                } else {
                    Log.e("AuthRepository", "Login Error: Empty response body")
                    Resource.Error("Empty response body from server.")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Login Error: ${response.code()} - $errorBody")
                Resource.Error(errorBody ?: "Login failed with code ${response.code()}")
            }
        } catch (e: HttpException) {
            Log.e("AuthRepository", "Login HTTP Exception: ${e.message()}", e)
            Resource.Error(e.message() ?: "HTTP error during login")
        } catch (e: IOException) {
            Log.e("AuthRepository", "Login Network Error: ${e.message}", e)
            Resource.Error("Network error: Check your internet connection.")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login Generic Exception: ${e.message}", e)
            Resource.Error("An unexpected error occurred during login.")
        }
    }

    suspend fun googleLogin(idToken: String): Resource<AuthResponse> {
        return try {
            Log.d("AuthRepository", "Attempting Google login with token.")
            // Assuming your backend expects idToken in a specific JSON structure
            val response = apiService.googleLogin(mapOf("id_token" to idToken)) // Example payload
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    Log.d("AuthRepository", "Google Login Success: ${authResponse.message}")
                    Resource.Success(authResponse)
                } else {
                    Log.e("AuthRepository", "Google Login Error: Empty response body")
                    Resource.Error("Empty response body from server.")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Google Login Error: ${response.code()} - $errorBody")
                Resource.Error(errorBody ?: "Google login failed with code ${response.code()}")
            }
        } catch (e: HttpException) {
            Log.e("AuthRepository", "Google Login HTTP Exception: ${e.message()}", e)
            Resource.Error(e.message() ?: "HTTP error during Google login")
        } catch (e: IOException) {
            Log.e("AuthRepository", "Google Login Network Error: ${e.message}", e)
            Resource.Error("Network error during Google login: Check your internet connection.")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google Login Generic Exception: ${e.message}", e)
            Resource.Error("An unexpected error occurred during Google login.")
        }
    }
}