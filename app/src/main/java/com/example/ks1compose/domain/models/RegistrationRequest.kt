package com.example.ks1compose.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequest(
    val login: String,
    val email: String,
    val password: String,
    val userName: String,
    val userSName: String,
    val userClass: String,
    val userSchool: String,
    val role: String = "student" // Добавляем роль
)



@Serializable
data class TokenResponse(
    val token: String,
    val userId: String,
    val role: String,
    val userName: String,
    val userSName: String
)