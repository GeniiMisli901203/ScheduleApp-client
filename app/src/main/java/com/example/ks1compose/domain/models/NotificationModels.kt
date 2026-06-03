package com.example.ks1compose.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class FcmTokenRequest(
    val token: String
)

@Serializable
data class NotificationDTO(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
    val createdAt: String
)