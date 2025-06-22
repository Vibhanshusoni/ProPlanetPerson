package com.example.proplanetperson.models

// This data class should be in com.example.proplanetperson.models/Quote.kt
data class Quote(
    val _id: String,
    val content: String, // This will be your quote text
    val author: String,  // This will be your quote author
    val tags: List<String>,
    val authorSlug: String,
    val length: Int,
    val dateAdded: String,
    val dateModified: String
)
