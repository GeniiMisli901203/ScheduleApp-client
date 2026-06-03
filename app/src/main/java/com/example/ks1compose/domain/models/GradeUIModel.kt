package com.example.ks1compose.domain.models

import androidx.compose.ui.graphics.Color
import com.example.ks1compose.data.DTOs.GradeDTO
import com.example.ks1compose.data.DTOs.LessonDTO


// Модель для отображения оценки в UI
data class GradeUIModel(
    val id: String,
    val studentId: String,
    val studentName: String,
    val subjectName: String,
    val gradeValue: Int,
    val gradeType: String,
    val teacherName: String,
    val comment: String?,
    val date: String,
    val color: Color
)

// Модель для отображения урока в UI
data class LessonUIModel(
    val id: String,
    val lessonNumber: Int,
    val subjectName: String,
    val teacherName: String?,
    val room: String?,
    val startTime: String?,
    val endTime: String?,
    val isCurrentLesson: Boolean = false,
    val dayOfWeek: String? = null,
    val className: String? = null
)

// Модель для отображения студента в UI
data class StudentUIModel(
    val id: String,
    val name: String,
    val className: String? = null,
    val averageGrade: Double? = null
)

// Модель для Dashboard
data class DashboardData(
    val userName: String,
    val userRole: String,
    val className: String? = null,
    val todayLessons: List<LessonUIModel> = emptyList(),
    val recentGrades: List<GradeUIModel> = emptyList(),
    val averageGrade: Double? = null,
    val newsCount: Int = 0
)

// Конвертер DTO -> UI Model
object ModelConverter {

    fun convertGradeToUIModel(grade: GradeDTO): GradeUIModel {
        val color = when (grade.gradeValue) {
            5 -> Color.Green
            4 -> Color(0xFF4CAF50)
            3 -> Color(0xFFFFC107)
            2 -> Color.Red
            else -> Color.Gray
        }

        return GradeUIModel(
            id = grade.gradeId ?: "",
            studentId = grade.studentId,
            studentName = grade.studentName ?: "Ученик",
            subjectName = grade.subjectName,
            gradeValue = grade.gradeValue,
            gradeType = grade.gradeType,
            teacherName = grade.teacherName ?: "Учитель",
            comment = grade.comment,
            date = grade.lessonDate,
            color = color
        )
    }

    fun convertLessonToUIModel(lesson: LessonDTO): LessonUIModel {
        return LessonUIModel(
            id = lesson.lessonId ?: "",
            lessonNumber = lesson.lessonNumber,
            subjectName = lesson.subjectName,
            teacherName = lesson.teacherName,
            room = lesson.room,
            startTime = lesson.startTime,
            endTime = lesson.endTime,
            isCurrentLesson = false,
            dayOfWeek = lesson.dayOfWeek,
            className = lesson.className
        )
    }

    fun convertUserToStudentModel(user: UserDTO): StudentUIModel {
        return StudentUIModel(
            id = user.userId,
            name = "${user.name ?: ""} ${user.sName ?: ""}".trim(),
            className = user.uClass,
            averageGrade = null
        )
    }
}