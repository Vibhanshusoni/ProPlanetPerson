package com.example.proplanetperson.api

import com.example.proplanetperson.models.Comment
import com.example.proplanetperson.models.Post
import com.example.proplanetperson.models.Story // Import Story
import com.example.proplanetperson.utils.Resource
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.IOException

class PostRepositoryImpl(private val postApi: PostApi) : PostRepository {

    // ... (existing getPostImage, getComments, postComment, createPost methods)

    override suspend fun getPostImage(postId: String, authToken: String): Resource<String> {
        return try {
            val response = postApi.getPostImage(postId, "Bearer $authToken")
            if (response.isSuccessful) {
                val imageUrl = response.body()?.get("imageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Resource.Success(imageUrl)
                } else {
                    Resource.Error("Post image URL not found in response.")
                }
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to fetch post image")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error fetching post image")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred while fetching post image")
        }
    }

    override suspend fun getComments(postId: String, authToken: String): Resource<List<Comment>> {
        return try {
            val response = postApi.getComments(postId, "Bearer $authToken")
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Comments not found or empty response")
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to fetch comments")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error fetching comments")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred while fetching comments")
        }
    }

    override suspend fun postComment(postId: String, authToken: String, publisherId: String, commentText: String): Resource<Comment> {
        return try {
            val commentData = mapOf(
                "publisher" to publisherId,
                "comment" to commentText
            )
            val response = postApi.postComment(postId, "Bearer $authToken", commentData)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Comment posted, but no data returned.")
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to post comment")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error posting comment")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred while posting comment")
        }
    }

    override suspend fun createPost(
        authToken: String,
        image: MultipartBody.Part,
        caption: RequestBody,
        publisherId: RequestBody
    ): Resource<Post> {
        return try {
            val response = postApi.createPost("Bearer $authToken", image, caption, publisherId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Post creation successful but no data returned.")
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to create post")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error creating post")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred during post creation")
        }
    }

    // --- NEW: Implement createStory method ---
    override suspend fun createStory(
        authToken: String,
        image: MultipartBody.Part,
        userId: RequestBody,
        timeEnd: RequestBody
    ): Resource<Story> {
        return try {
            val response = postApi.createStory("Bearer $authToken", image, userId, timeEnd)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Story creation successful but no data returned.")
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to create story")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error creating story")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred during story creation")
        }
    }
    // --- END NEW ---
}