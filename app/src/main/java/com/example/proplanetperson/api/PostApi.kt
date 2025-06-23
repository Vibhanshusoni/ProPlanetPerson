package com.example.proplanetperson.api

import com.example.proplanetperson.models.Comment
import com.example.proplanetperson.models.Post
import com.example.proplanetperson.models.Story // Import Story model
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PostApi {

    @GET("api/posts/{postId}/image")
    suspend fun getPostImage(
        @Path("postId") postId: String,
        @Header("Authorization") authToken: String
    ): Response<Map<String, String>>

    @GET("api/posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: String,
        @Header("Authorization") authToken: String
    ): Response<List<Comment>>

    @POST("api/posts/{postId}/comments")
    suspend fun postComment(
        @Path("postId") postId: String,
        @Header("Authorization") authToken: String,
        @Body commentData: Map<String, String>
    ): Response<Comment>

    @Multipart
    @POST("api/posts") // This is for general posts.
    suspend fun createPost(
        @Header("Authorization") authToken: String,
        @Part image: MultipartBody.Part,
        @Part("caption") caption: RequestBody,
        @Part("publisherId") publisherId: RequestBody
    ): Response<Post>

    // --- NEW: Endpoint for creating a new Story with an image upload ---
    @Multipart
    @POST("api/stories") // <--- DEFINE YOUR ACTUAL CREATE STORY ENDPOINT
    suspend fun createStory(
        @Header("Authorization") authToken: String,
        @Part image: MultipartBody.Part,
        @Part("userId") userId: RequestBody, // The ID of the user creating the story
        @Part("timeEnd") timeEnd: RequestBody // The end time for the story's visibility
    ): Response<Story> // Assuming your backend returns the created Story object
    // Or Response<Map<String, String>> if it just returns success message/image URL.
    // Adjust Story to match the expected response if it's different.
    // --- END NEW ---
}