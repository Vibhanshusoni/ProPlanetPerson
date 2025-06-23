package com.example.proplanetperson.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proplanetperson.api.AuthRepositoryImpl
import com.example.proplanetperson.models.AuthResponse
import com.example.proplanetperson.models.User
import com.example.proplanetperson.models.UserAuthRequest
import com.example.proplanetperson.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepositoryImpl) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _authResult = MutableLiveData<Resource<AuthResponse>>()
    val authResult: LiveData<Resource<AuthResponse>> = _authResult

    fun registerUser(request: UserAuthRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _authResult.value = Resource.Loading()
            val result = repository.registerUser(request)
            _authResult.value = result
            _isLoading.value = false
        }
    }

    fun loginUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _authResult.value = Resource.Loading()
            val result = repository.loginUser(user)
            _authResult.value = result
            _isLoading.value = false
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authResult.value = Resource.Loading()
            val result = repository.googleLogin(idToken)
            _authResult.value = result
            _isLoading.value = false
        }
    }

    fun resetAuthResult() {
        // CORRECTED LINE: Instantiate Idle as a class
        _authResult.value = Resource.Idle()
    }
}