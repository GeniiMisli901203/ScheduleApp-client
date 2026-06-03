package com.example.ks1compose.data.DTOs

import com.example.ks1compose.domain.models.UserDTO
import kotlinx.serialization.Serializable

@Serializable
data class StudentsListResponse(
    val success: Boolean,
    val message: String? = null,
    val students: List<UserDTO>? = null
)