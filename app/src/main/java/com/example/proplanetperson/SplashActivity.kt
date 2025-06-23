package com.example.proplanetperson // Your package name

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.proplanetperson.utils.SessionManager // Import SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager // Declare SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You might have setContentView(R.layout.activity_splash) here if you have a splash layout
        // For a simple splash screen, setting a background on the theme can often suffice,
        // so setContentView might not be strictly necessary if just showing a logo for a few seconds.

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        // Removed: FirebaseApp.initializeApp(this) - Not needed if not using Firebase services
        // Removed: import com.google.firebase.FirebaseApp
        // Removed: import com.google.firebase.auth.FirebaseAuth

        Handler(Looper.getMainLooper()).postDelayed({
            // Check login status using your SessionManager
            if (sessionManager.isLoggedIn()) {
                // User is already logged in according to your session
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // User is not logged in, or session expired, go to LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish() // Close SplashActivity
        }, 3000) // Delay for 3 seconds
    }
}