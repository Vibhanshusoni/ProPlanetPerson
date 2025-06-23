package com.example.proplanetperson.data.repository

import com.example.proplanetperson.models.AuthResponse
import com.example.proplanetperson.models.ErrorResponse
import com.example.proplanetperson.models.UserAuthRequest
import com.example.proplanetperson.data.remote.AuthApiService
import com.example.proplanetperson.utils.Resource
import com.google.gson.Gson // For parsing error bodies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(private val authApiService: AuthApiService) {

    suspend fun registerUser(request: UserAuthRequest): Resource<AuthResponse> {
        return safeApiCall { authApiService.registerUser(request) }
    }

    suspend fun loginUser(request: UserAuthRequest): Resource<AuthResponse> {
        return safeApiCall { authApiService.loginUser(request) }
    }

    // Generic safe API call wrapper to handle common network and HTTP errors
    private suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): Resource<T> {
        return withContext(Dispatchers.IO) { // Perform network call on IO dispatcher
            try {
                val response = apiCall.invoke()
                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        Resource.Success(data)
                    } ?: Resource.Error("API call successful but returned null body")
                } else {
                    // Attempt to parse the error body from the backend
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java)?.message
                    } catch (e: Exception) {
                        null
                    }

                    Resource.Error(errorMessage ?: "Error: ${response.code()} ${response.message()}")
                }
            } catch (e: HttpException) {
                // HTTP errors (e.g., 400, 401, 404, 500)
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java)?.message
                } catch (ex: Exception) {
                    null
                }
                Resource.Error(errorMessage ?: "Network Error: ${e.code()} ${e.message()}")
            } catch (e: IOException) {
                // Network errors (no internet, timeout, DNS issues)
                Resource.Error("Network Error: Please check your internet connection.")
            } catch (e: Exception) {
                // Any other unexpected errors
                Resource.Error("An unexpected error occurred: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }
}