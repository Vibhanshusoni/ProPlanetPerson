package com.example.proplanetperson.api

import android.util.Log
import com.example.proplanetperson.models.AuthResponse
import com.example.proplanetperson.models.User
import com.example.proplanetperson.models.UserAuthRequest // Assuming you have this model
import com.example.proplanetperson.utils.Resource
import retrofit2.HttpException
import java.io.IOException

// Assuming you have an ApiService interface defined
// interface ApiService {
//     @POST("api/register")
//     suspend fun registerUser(@Body request: UserAuthRequest): Response<AuthResponse>
//
//     @POST("api/login")
//     suspend fun loginUser(@Body user: User): Response<AuthResponse> // Or whatever your login payload is
//
//     @POST("api/google-login")
//     suspend fun googleLogin(@Body idToken: Map<String, String>): Response<AuthResponse>
// }

class AuthRepository(private val apiService: ApiService) { // ApiService injected

    // This function was originally inside AuthViewModel, now correctly in AuthRepository
    suspend fun registerUser(request: UserAuthRequest): Resource<AuthResponse> {
        return try {
            Log.d("AuthRepository", "Attempting registration request for email: ${request.email}")
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

    // This function was originally inside AuthViewModel, now correctly in AuthRepository
    suspend fun loginUser(user: User): Resource<AuthResponse> {
        return try {
            Log.d("AuthRepository", "Attempting login request for email: ${user.email}")
            val response = apiService.loginUser(user) // Use the injected apiService
            Log.d("AuthRepository", "Login API response received: ${response.code()}")
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
        } catch (e: HttpException) { // Catch Retrofit HTTP exceptions
            Log.e("AuthRepository", "Login HTTP Exception: ${e.message()}", e)
            Resource.Error(e.message() ?: "HTTP error during login")
        } catch (e: IOException) { // Catch network-related exceptions
            Log.e("AuthRepository", "Login Network Error: ${e.message}", e)
            Resource.Error("Network error: Check your internet connection.")
        } catch (e: Exception) { // Catch any other unexpected exceptions
            Log.e("AuthRepository", "Login Generic Exception: ${e.message}", e)
            Resource.Error("An unexpected error occurred during login.")
        }
    }

    // Example for googleLogin in repository
    suspend fun googleLogin(idToken: String): Resource<AuthResponse> {
        return try {
            Log.d("AuthRepository", "Attempting Google login with token.")
            // Assuming your backend expects idToken in a specific JSON structure
            val response = apiService.googleLogin(mapOf("id_token" to idToken))
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