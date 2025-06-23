package com.example.proplanetperson.models

data class UserAuthRequest(
    val email: String,
    val password: String,
    val fullname: String, // Add this
    val username: String, // Add this
    val bio: String,      // Add this
    val image: String = "" // Add this, with a default empty string for new registrations if no image is uploaded initially
)

// The registerUser function snippet would remain similar,
// but it would now expect an updated UserAuthRequest object:

/*
// Example of how your ViewModel's registerUser might look (assuming this is from a ViewModel)
// This snippet would be in a class like AuthViewModel or RegisterViewModel
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _authResult = MutableLiveData<Resource<User>>() // Assuming Resource<User> for result
    val authResult: LiveData<Resource<User>> = _authResult

    fun registerUser(request: UserAuthRequest) { // Now expects fullname, username, bio, image
        viewModelScope.launch {
            _isLoading.value = true
            _authResult.value = Resource.Loading()

            val result = repository.registerUser(request) // Your repository's method also needs to accept these fields
            _authResult.value = result
            _isLoading.value = false
        }
    }
}
*/