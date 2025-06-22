package com.example.proplanetperson // Your package name

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp // Import this
import com.google.firebase.auth.FirebaseAuth // Import this

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You might have setContentView(R.layout.activity_splash) here if you have a splash layout

        // Initialize FirebaseApp here. This is crucial for SplashActivity as it's the first activity.
        // It acts as a safeguard in case MyApplication.onCreate() hasn't fully completed yet.
        FirebaseApp.initializeApp(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser // Access Firebase after initialization
            if (currentUser != null) {
                // User is already logged in, go to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // User is not logged in, go to LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish() // Close SplashActivity
        }, 3000) // Delay for 3 seconds
    }
}