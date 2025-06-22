package com.example.proplanetperson

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.proplanetperson.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.Fragment
// Removed FirebaseApp import as initialization will be in custom Application class
// import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Removed FirebaseApp.initializeApp(this) from here.
        // It will now be initialized in the custom Application class for earlier setup.

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // Setup Drawer Toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set default fragment
        loadFragment(HomeFragment())

        // Bottom Navigation setup
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_upload -> loadFragment(UploadPostFragment())
                R.id.nav_leaderboard -> loadFragment(LeaderboardFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
                R.id.nav_search -> loadFragment(SearchFragment())
                else -> false
            }
            true
        }

        // Side Navigation setup
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> loadFragment(SettingFragment())
                R.id.nav_buy -> loadFragment(BuyFragment())
                R.id.nav_sell -> loadFragment(SellFragment())
                R.id.nav_history -> loadFragment(HistoryFragment())
                R.id.nav_notifications -> loadFragment(NotificationFragment())
                R.id.nav_educational -> loadFragment(EducationalFragment())
                R.id.nav_theme -> toggleTheme()
                R.id.nav_signout -> showSignOutDialog()
                else -> Toast.makeText(this, "Unknown item clicked", Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navView)) {
            drawerLayout.closeDrawer(navView)
            return
        }

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment !is HomeFragment) {
            loadFragment(HomeFragment())
            findViewById<BottomNavigationView>(R.id.bottom_nav).selectedItemId = R.id.nav_home
        } else {
            super.onBackPressed()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun toggleTheme() {
        val isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        )
        // Optional: Persist using SharedPreferences if needed
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
