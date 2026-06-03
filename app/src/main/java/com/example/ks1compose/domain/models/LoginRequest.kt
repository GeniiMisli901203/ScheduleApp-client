package com.example.ks1compose.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val login: String,
    val password: String
)