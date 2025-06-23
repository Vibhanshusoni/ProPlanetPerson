package com.example.proplanetperson

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.proplanetperson.api.ApiClient
import com.example.proplanetperson.api.ApiService
import com.example.proplanetperson.api.AuthRepository // Now correctly refers to your AuthRepository class
import com.example.proplanetperson.api.AuthRepositoryImpl
import com.example.proplanetperson.models.User // User model for login payload
import com.example.proplanetperson.ui.auth.AuthViewModel
import com.example.proplanetperson.ui.auth.AuthViewModelFactory
import com.example.proplanetperson.utils.Resource
import com.example.proplanetperson.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize UI elements
        emailEditText = findViewById(R.id.loginUserName)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpTextView = findViewById(R.id.signup) // Assuming this is your "Sign Up" TextView ID

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize ViewModel
        val repository = AuthRepositoryImpl(ApiClient.authApi as ApiService) // <--- CHANGE THIS LINE to use AuthRepositoryImpl
        val viewModelFactory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)

        // Observe the authentication result LiveData
        authViewModel.authResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    Log.d("LoginActivity", "Login state: Loading")
                    Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
                    loginButton.isEnabled = false
                }
                is Resource.Success -> {
                    loginButton.isEnabled = true
                    Log.d("LoginActivity", "Login state: Success. User ID: ${result.data?.userId}")
                    Toast.makeText(this, result.data?.message ?: "Login successful!", Toast.LENGTH_SHORT).show()

                    result.data?.let { authResponse ->
                        sessionManager.login(authResponse.userId ?: "", authResponse.token)
                    }

                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    loginButton.isEnabled = true
                    Log.e("LoginActivity", "Login state: Error - ${result.message}")
                    Toast.makeText(this, result.message ?: "Login failed.", Toast.LENGTH_LONG).show()
                }
                is Resource.Idle -> {
                    Log.d("LoginActivity", "Login state: Idle")
                }
            }
        }

        // Set up login button click listener
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            } else {
                val loginUserPayload = User(email = email, password = password) // Assuming User has 'email' and 'password' params
                authViewModel.loginUser(loginUserPayload)
            }
        }

        // Set up listener to go to SignUpActivity
        signUpTextView.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }
}

// REMOVE THIS STUB FUNCTION if it's still present in your file. It causes conflicts.
// private fun SessionManager.saveAuthToken(string: String) {}