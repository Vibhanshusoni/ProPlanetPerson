package com.example.proplanetperson.api

import com.example.proplanetperson.models.Quote
import com.example.proplanetperson.utils.Resource

interface QuoteRepository {
    suspend fun getRandomQuote(): Resource<Quote>
}