package com.example.proplanetperson.api

import com.example.proplanetperson.models.Quote
import com.example.proplanetperson.utils.Resource
import retrofit2.HttpException
import java.io.IOException

class QuoteRepositoryImpl(private val quoteApi: QuoteApi) : QuoteRepository {
    override suspend fun getRandomQuote(): Resource<Quote> {
        return try {
            val response = quoteApi.getRandomQuote()
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Quote response body is null")
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Failed to fetch quote")
            }
        } catch (e: HttpException) {
            Resource.Error(e.localizedMessage ?: "Network error fetching quote")
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred while fetching quote")
        }
    }
}