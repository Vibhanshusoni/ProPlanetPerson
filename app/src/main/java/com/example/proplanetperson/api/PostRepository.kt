package com.example.proplanetperson.api

import com.example.proplanetperson.models.Comment
import com.example.proplanetperson.models.Post
import com.example.proplanetperson.models.Story // Import Story model
import com.example.proplanetperson.utils.Resource
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface PostRepository {
    suspend fun getPostImage(postId: String, authToken: String): Resource<String>
    suspend fun getComments(postId: String, authToken: String): Resource<List<Comment>>
    suspend fun postComment(postId: String, authToken: String, publisherId: String, commentText: String): Resource<Comment>
    suspend fun createPost(authToken: String, image: MultipartBody.Part, caption: RequestBody, publisherId: RequestBody): Resource<Post>

    // --- NEW: Method for creating a new story ---
    suspend fun createStory(
        authToken: String,
        image: MultipartBody.Part,
        userId: RequestBody,
        timeEnd: RequestBody
    ): Resource<Story> // Returns the created Story object
    // --- END NEW ---
}