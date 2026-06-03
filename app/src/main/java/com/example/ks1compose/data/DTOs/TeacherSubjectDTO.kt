package com.example.ks1compose.data.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class TeacherSubjectDTO(
    val subjectId: String? = null,
    val teacherId: String,
    val teacherName: String? = null,
    val subjectName: String,
    val className: String
)

@Serializable
data class TeacherSubjectsResponse(
    val success: Boolean,
    val subjects: List<Map<String, String>>? = null
)

@Serializable
data class AddSubjectRequest(
    val teacherId: String,
    val subjectName: String,
    val className: String
)