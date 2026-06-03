package com.example.ks1compose.data.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleDTO(
    val scheduleId: String = "",
    val className: String,
    val day: String,
    val lessons: List<String>,
    val office: List<String>
)

@Serializable
data class ScheduleResponse(
    val success: Boolean,
    val message: String? = null,
    val schedules: List<ScheduleDTO>? = null
)