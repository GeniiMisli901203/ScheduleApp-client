package com.example.ks1compose.data.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class NewsDTO(
    val userId: String,
    val title: String,
    val description: String,
    val url: String? = null,
    val createdAt: String? = null
)

@Serializable
data class NewsResponse(
    val success: Boolean,
    val message: String? = null,
    val newsList: List<NewsDTO>? = null
)