package com.example.proplanetperson

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proplanetperson.api.ApiClient
import com.example.proplanetperson.api.UserRepositoryImpl // Used in ViewModel factory
import com.example.proplanetperson.models.LoginRequest
import com.example.proplanetperson.models.LoginResponse
import com.example.proplanetperson.models.ServerStatus // Import if you use it directly
import com.example.proplanetperson.ui.main.MainViewModel // Import the new ViewModel
import com.example.proplanetperson.utils.Resource // Import Resource class

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Your layout file

        // Initialize MainViewModel
        val userRepository = UserRepositoryImpl(ApiClient.userApi)
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(userRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)


        // --- Observe LiveData from MainViewModel ---

        // Observe server status
        mainViewModel.serverStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Toast.makeText(this, "Checking server status...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    val serverStatus = resource.data
                    Log.d("ConnectionTest", "Server status: ${serverStatus?.message}")
                    Toast.makeText(this, "Connected: ${serverStatus?.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Error -> {
                    val errorMessage = resource.message ?: "Unknown error"
                    Log.e("ConnectionTest", "Failed to connect: $errorMessage")
                    Toast.makeText(this, "Connection failed: $errorMessage", Toast.LENGTH_LONG).show()
                }
                is Resource.Idle -> {
                    // Do nothing, or reset UI elements if needed when idle
                    Log.d("ConnectionTest", "Server status check is idle.")
                }
            }
        }

        // Observe login result
        mainViewModel.loginResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    val loginResponse = resource.data
                    Log.d("ConnectionTest", "Login successful: ${loginResponse?.message}, User ID: ${loginResponse?.userId}")
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_LONG).show()
                    // Here you would typically save the token and navigate to the next activity
                }
                is Resource.Error -> {
                    val errorMessage = resource.message ?: "Login failed"
                    Log.e("ConnectionTest", "Login failed: $errorMessage")
                    Toast.makeText(this, "Login failed: $errorMessage", Toast.LENGTH_LONG).show()
                }
                is Resource.Idle -> { // <--- This is the one you need to keep with the Idle branch
                    // Do nothing, or reset login UI state
                    Log.d("LoginTest", "Login state is idle.")
                }
            }
        }

        // --- Trigger API calls via ViewModel ---

        // Example: Make a call to the status endpoint
        mainViewModel.fetchServerStatus()

        // Example: Make a login call (you might want to trigger this from a button click)
        // For demonstration, it's here:
        val loginRequest = LoginRequest("testuser", "testpass") // Replace with actual user input
        mainViewModel.performLogin(loginRequest)
    }
}