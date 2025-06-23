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
import com.example.proplanetperson.api.ApiClient.authApi
import com.example.proplanetperson.api.AuthRepositoryImpl
import com.example.proplanetperson.models.UserAuthRequest
import com.example.proplanetperson.ui.auth.AuthViewModel
import com.example.proplanetperson.ui.auth.AuthViewModelFactory // Make sure to import this
import com.example.proplanetperson.utils.Resource
import com.example.proplanetperson.utils.SessionManager

class SignUpActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    // Declare EditTexts and Button matching XML IDs
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginTextView: TextView // Renamed to avoid confusion with actual signup logic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Initialize AuthViewModel using the factory
        val authRepository = AuthRepositoryImpl(authApi) // Use AuthRepositoryImpl and ApiClient.authApi
        val viewModelFactory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)

        // Get views using correct XML IDs
        emailEditText = findViewById(R.id.username) // Email field
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.Confirm_password)
        signUpButton = findViewById(R.id.signupButton)
        loginTextView = findViewById(R.id.signup) // This is the "Already have an account? Log In" TextView

        // Observe authentication results from AuthViewModel
        authViewModel.authResult.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    Toast.makeText(this, "Signing up...", Toast.LENGTH_SHORT).show()
                    signUpButton.isEnabled = false // Disable button during loading
                }
                is Resource.Success -> {
                    signUpButton.isEnabled = true // Re-enable button
                    val authResponse = result.data
                    if (authResponse != null) {
                        // Successfully registered and backend likely returns auth token
                        sessionManager.login(authResponse.userId, authResponse.token) // Save userId and token
                        Toast.makeText(this, authResponse.message ?: "Sign-Up Successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to MainActivity after successful sign-up and session save
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Sign-Up Failed: Empty response", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    signUpButton.isEnabled = true // Re-enable button
                    Toast.makeText(this, "Sign-Up Failed: ${result.message}", Toast.LENGTH_LONG).show()
                    Log.e("SignUpActivity", "Sign-Up Error: ${result.message}")
                }
                is Resource.Idle -> {
                    Log.d("SignUpActivity", "Auth result state is idle.")
                    // You might re-enable UI elements here if they were disabled, or reset state
                }
            }
        }

        // 🔁 Redirect to Login screen
        loginTextView.setOnClickListener { // Use the renamed TextView variable
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 🔐 Sign-up logic
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim() // Get email from the 'username' EditText
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // *** IMPORTANT: Your activity_sign_up.xml only has fields for email, password, confirm password. ***
            // *** It does NOT have separate EditTexts for fullname, username (if different from email), or bio. ***
            // *** If your backend requires these for registration, you MUST add EditTexts for them in your XML ***
            // *** and retrieve their values here. For now, I'll pass empty strings for them. ***
            val signupUsername = email // Assuming username for registration is the same as email for now
            val signupFullname = "" // Will be empty unless you add EditText in XML
            val signupBio = ""      // Will be empty unless you add EditText in XML
            val signupImage = ""    // Will be empty unless you add image selection

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                // Create a UserAuthRequest object for registration with available data
                val registerRequest = UserAuthRequest(
                    email = email,
                    password = password,
                    username = signupUsername,
                    fullname = signupFullname,
                    bio = signupBio,
                    image = signupImage
                )
                authViewModel.registerUser(registerRequest) // Call the ViewModel
            }
        }
    }
}