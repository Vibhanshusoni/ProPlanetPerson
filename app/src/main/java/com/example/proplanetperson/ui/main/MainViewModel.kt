package com.example.proplanetperson.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proplanetperson.api.UserRepository
import com.example.proplanetperson.models.LoginRequest
import com.example.proplanetperson.models.LoginResponse
import com.example.proplanetperson.models.ServerStatus
import com.example.proplanetperson.utils.Resource // Ensure Resource is imported
import kotlinx.coroutines.launch

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _serverStatus = MutableLiveData<Resource<ServerStatus>>()
    val serverStatus: LiveData<Resource<ServerStatus>> = _serverStatus

    private val _loginResult = MutableLiveData<Resource<LoginResponse>>()
    val loginResult: LiveData<Resource<LoginResponse>> = _loginResult

    fun fetchServerStatus() {
        _serverStatus.value = Resource.Loading()
        viewModelScope.launch {
            _serverStatus.value = userRepository.getServerStatus()
        }
    }

    fun performLogin(request: LoginRequest) {
        _loginResult.value = Resource.Loading()
        viewModelScope.launch {
            _loginResult.value = userRepository.loginUser(request)
        }
    }

    fun resetLoginResult() {
        _loginResult.value = Resource.Idle // <--- CHANGE THIS LINE
    }
}