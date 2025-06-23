package com.example.proplanetperson.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proplanetperson.api.AuthRepositoryImpl // Use AuthRepositoryImpl here

class AuthViewModelFactory(private val repository: AuthRepositoryImpl) : ViewModelProvider.Factory { // Use AuthRepositoryImpl here
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}