package com.example.ks1compose.data.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class LessonDTO(
    val lessonId: String? = null,
    val className: String,
    val dayOfWeek: String, // "monday", "tuesday", etc.
    val weekNumber: Int? = null,
    val lessonNumber: Int,
    val subjectName: String,
    val teacherId: String? = null,
    val teacherName: String? = null,
    val room: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)

@Serializable
data class LessonsResponse(
    val success: Boolean,
    val message: String? = null,
    val lessons: List<LessonDTO>? = null
)

@Serializable
data class WeeklyScheduleDTO(
    val className: String,
    val monday: List<LessonDTO> = emptyList(),
    val tuesday: List<LessonDTO> = emptyList(),
    val wednesday: List<LessonDTO> = emptyList(),
    val thursday: List<LessonDTO> = emptyList(),
    val friday: List<LessonDTO> = emptyList(),
    val saturday: List<LessonDTO> = emptyList()
)

@Serializable
data class WeeklyScheduleResponse(
    val success: Boolean,
    val schedule: WeeklyScheduleDTO? = null
)