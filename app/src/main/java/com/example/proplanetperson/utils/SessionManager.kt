package com.example.proplanetperson.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val PREF_NAME = "UserSession"
    private val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val KEY_USER_ID = "userId"
    private val KEY_AUTH_TOKEN = "authToken" // Optional: if your backend uses tokens

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    /**
     * Logs in the user by saving their ID and marking them as logged in.
     * @param userId The unique ID of the logged-in user.
     * @param authToken (Optional) The authentication token received from the backend.
     */
    fun login(userId: String, authToken: String? = null) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_ID, userId)
        authToken?.let { editor.putString(KEY_AUTH_TOKEN, it) }
        editor.apply() // Use apply() for asynchronous saving
    }

    /**
     * Logs out the user by clearing all session data.
     */
    fun logout() {
        editor.clear()
        editor.apply()
    }

    /**
     * Checks if a user is currently logged in.
     * @return true if a user is logged in, false otherwise.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Retrieves the ID of the currently logged-in user.
     * @return The user ID as a String, or null if no user is logged in.
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    /**
     * Retrieves the authentication token for the currently logged-in user.
     * @return The authentication token as a String, or null if no token is available.
     */
    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }
}