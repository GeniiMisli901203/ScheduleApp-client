package com.example.ks1compose.data.DTOs

import kotlinx.serialization.Serializable


@Serializable
data class GradeDTO(
    val gradeId: String? = null,
    val studentId: String,
    val studentName: String? = null,
    val teacherId: String,
    val teacherName: String? = null,
    val subjectName: String,
    val className: String,
    val gradeValue: Int,
    val gradeType: String,
    val comment: String? = null,
    val lessonDate: String,
    val createdAt: String? = null
)

@Serializable
data class GradeResponse(
    val success: Boolean,
    val message: String? = null,
    val grades: List<GradeDTO>? = null,
    val averageGrade: Double? = null
)

@Serializable
data class UpdateGradeRequest(
    val gradeValue: Int? = null,
    val comment: String? = null
)