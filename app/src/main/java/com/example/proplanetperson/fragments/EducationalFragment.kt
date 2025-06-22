package com.example.proplanetperson.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proplanetperson.R
import com.example.proplanetperson.adapters.VideoAdapter
import com.example.proplanetperson.api.YouTubeApiClient
import com.example.proplanetperson.api.YouTubeApiService
import com.example.proplanetperson.models.YouTubeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EducationalFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var youtubeApiService: YouTubeApiService
    private val apiKey = "YAIzaSyCwkipci342gvwN4B2lAHvUbwxsJx854nc" // Replace with your API key

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_educational, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewEducational)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        youtubeApiService = YouTubeApiClient.retrofit.create(YouTubeApiService::class.java)

        fetchEducationalVideos("how to grow plants")

        return view
    }

    private fun fetchEducationalVideos(query: String) {
        youtubeApiService.searchVideos(query, apiKey).enqueue(object : Callback<YouTubeResponse> {
            override fun onResponse(
                call: Call<YouTubeResponse>,
                response: Response<YouTubeResponse>
            ) {
                if (response.isSuccessful) {
                    val videoList = response.body()?.items ?: emptyList()
                    recyclerView.adapter = VideoAdapter(videoList)
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch videos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<YouTubeResponse>, t: Throwable) {
                Log.e("EducationalFragment", "YouTube API Error: ${t.message}")
                Toast.makeText(requireContext(), "An error occurred", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
