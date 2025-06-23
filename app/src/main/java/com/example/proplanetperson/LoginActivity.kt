package com.example.proplanetperson

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button // Assuming you have a login button
import android.widget.EditText // Assuming you have email/password input fields
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.proplanetperson.MainActivity // Your main activity after login
import com.example.proplanetperson.R // Your layout file
import com.example.proplanetperson.api.ApiClient // Assuming you have an ApiClient to get ApiService
import com.example.proplanetperson.api.AuthRepository
import com.example.proplanetperson.models.User // User model for login payload
import com.example.proplanetperson.ui.auth.AuthViewModel
import com.example.proplanetperson.ui.auth.AuthViewModelFactory
import com.example.proplanetperson.utils.Resource // Your Resource class
import com.example.proplanetperson.utils.SessionManager // Your SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager // To save session after login

    // Example UI elements (replace with your actual IDs)
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Replace with your actual layout

        // Initialize UI elements
        emailEditText = findViewById(R.id.loginUserName) // Replace with your actual IDs
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize ViewModel
        // You'll need a ViewModelFactory if your ViewModel has constructor arguments
        val repository = AuthRepository(ApiClient.apiService) // Get your ApiService here
        val viewModelFactory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)

        // Observe the authentication result LiveData
        authViewModel.authResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    Log.d("LoginActivity", "Login state: Loading")
                    // Show loading indicator (e.g., ProgressBar)
                }
                is Resource.Success -> {
                    Log.d("LoginActivity", "Login state: Success. User ID: ${result.data?.userId}")
                    // Hide loading indicator
                    Toast.makeText(this, result.data?.message ?: "Login successful!", Toast.LENGTH_SHORT).show()

                    // Save user session (token, userId, etc.) using SessionManager
                    result.data?.let {
                        sessionManager.saveAuthToken(it.token)
                        sessionManager.saveUserId(it.userId)
                        // Save other user info if your AuthResponse contains it
                    }

                    // Navigate to the main activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Close LoginActivity so user cannot go back to it
                }
                is Resource.Error -> {
                    Log.e("LoginActivity", "Login state: Error - ${result.message}")
                    // Hide loading indicator
                    Toast.makeText(this, result.message ?: "Login failed.", Toast.LENGTH_LONG).show()
                }
                is Resource.Idle -> {
                    Log.d("LoginActivity", "Login state: Idle")
                    // Initial state or after a reset
                }
            }
        }

        // Set up login button click listener
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Assuming your backend login expects a User object with email and password
                val loginUserPayload = User(email = email, password = password)
                authViewModel.loginUser(loginUserPayload)
            } else {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun SessionManager.saveAuthToken(string: String) {}
