package com.example.proplanetperson.ui.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proplanetperson.api.PostRepository
import com.example.proplanetperson.api.UserRepository
import com.example.proplanetperson.models.Comment
import com.example.proplanetperson.models.Post
import com.example.proplanetperson.models.Story // Import Story model
import com.example.proplanetperson.models.User
import com.example.proplanetperson.utils.Resource // Ensure Resource is imported
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PostViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _comments = MutableLiveData<Resource<List<Comment>>>()
    val comments: LiveData<Resource<List<Comment>>> = _comments

    private val _postImage = MutableLiveData<Resource<String>>()
    val postImage: LiveData<Resource<String>> = _postImage

    private val _userProfileImage = MutableLiveData<Resource<String>>()
    val userProfileImage: LiveData<Resource<String>> = _userProfileImage

    private val _postCommentResult = MutableLiveData<Resource<Comment>>()
    val postCommentResult: LiveData<Resource<Comment>> = _postCommentResult

    private val _createPostResult = MutableLiveData<Resource<Post>>()
    val createPostResult: LiveData<Resource<Post>> = _createPostResult

    // --- NEW: LiveData for story creation result ---
    private val _createStoryResult = MutableLiveData<Resource<Story>>()
    val createStoryResult: LiveData<Resource<Story>> = _createStoryResult
    // --- END NEW ---

    fun getComments(postId: String, authToken: String) {
        _comments.value = Resource.Loading()
        viewModelScope.launch {
            _comments.value = postRepository.getComments(postId, authToken)
        }
    }

    fun getPostImage(postId: String, authToken: String) {
        _postImage.value = Resource.Loading()
        viewModelScope.launch {
            _postImage.value = postRepository.getPostImage(postId, authToken)
        }
    }

    fun getCurrentUserProfileImage(userId: String, authToken: String) {
        _userProfileImage.value = Resource.Loading()
        viewModelScope.launch {
            when (val result = userRepository.getUserProfile(userId, authToken)) {
                is Resource.Success -> {
                    // Assuming User model has an 'image' property for profile URL
                    _userProfileImage.value = Resource.Success(result.data?.image ?: "")
                }
                is Resource.Error -> {
                    _userProfileImage.value = Resource.Error(result.message ?: "Failed to load user profile image")
                }
                is Resource.Loading -> { /* Handled by initial Resource.Loading() above */ }
                is Resource.Idle -> { /* No action needed for Idle state */ } // Ensure all Resource states are handled
            }
        }
    }

    fun postComment(postId: String, authToken: String, publisherId: String, commentText: String) {
        _postCommentResult.value = Resource.Loading()
        viewModelScope.launch {
            _postCommentResult.value = postRepository.postComment(postId, authToken, publisherId, commentText)
        }
    }

    fun createPost(authToken: String, image: MultipartBody.Part, caption: RequestBody, publisherId: RequestBody) {
        _createPostResult.value = Resource.Loading()
        viewModelScope.launch {
            _createPostResult.value = postRepository.createPost(authToken, image, caption, publisherId)
        }
    }

    // --- NEW: Function to create a new story ---
    fun createStory(authToken: String, image: MultipartBody.Part, userId: RequestBody, timeEnd: RequestBody) {
        _createStoryResult.value = Resource.Loading()
        viewModelScope.launch {
            _createStoryResult.value = postRepository.createStory(authToken, image, userId, timeEnd)
        }
    }

    // Reset function for createStoryResult
    fun resetCreateStoryResult() {
        _createStoryResult.value = Resource.Idle // Correctly reset to Resource.Idle
    }
    // --- END NEW ---

    fun resetCreatePostResult() {
        _createPostResult.value = Resource.Idle // Correctly reset to Resource.Idle
    }

    fun resetPostCommentResult() {
        _postCommentResult.value = Resource.Idle // Correctly reset to Resource.Idle
    }
}