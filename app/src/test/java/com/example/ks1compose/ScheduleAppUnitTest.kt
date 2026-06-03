package com.example.ks1compose

import com.example.ks1compose.data.DTOs.GradeDTO
import com.example.ks1compose.data.DTOs.LessonDTO
import com.example.ks1compose.domain.models.ModelConverter
import com.example.ks1compose.domain.models.UserDTO
import org.junit.Test
import org.junit.Assert.*


class ScheduleAppUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testGradeCalculation_averageOfFiveAndFour() {
        val grades = listOf(5, 4)
        val average = grades.average()
        assertEquals(4.5, average, 0.01)
    }

    @Test
    fun testGradeCalculation_averageOfFiveFiveAndThree() {
        val grades = listOf(5, 5, 3)
        val average = grades.average()
        assertEquals(4.33, average, 0.01)
    }

    @Test
    fun testGradeCalculation_allFives() {
        val grades = listOf(5, 5, 5, 5)
        val average = grades.average()
        assertEquals(5.0, average, 0.01)
    }

    @Test
    fun testGradeCalculation_allTwos() {
        val grades = listOf(2, 2, 2)
        val average = grades.average()
        assertEquals(2.0, average, 0.01)
    }

    @Test
    fun testGradeCalculation_emptyList() {
        val grades = emptyList<Int>()
        val average = grades.average()
        assertTrue(average.isNaN())
    }

    @Test
    fun testConvertGradeToUIModel() {
        val gradeDTO = GradeDTO(
            gradeId = "123",
            studentId = "student1",
            studentName = "Иван Петров",
            teacherId = "teacher1",
            teacherName = "Мария Иванова",
            subjectName = "Математика",
            className = "10А",
            gradeValue = 5,
            gradeType = "classwork",
            comment = "Отлично!",
            lessonDate = "2024-02-28",
            createdAt = "2024-02-28"
        )

        val uiModel = ModelConverter.convertGradeToUIModel(gradeDTO)

        assertEquals("123", uiModel.id)
        assertEquals("Математика", uiModel.subjectName)
        assertEquals(5, uiModel.gradeValue)
        assertEquals("Отлично!", uiModel.comment)
    }

    @Test
    fun testConvertLessonToUIModel() {
        val lessonDTO = LessonDTO(
            lessonId = "lesson1",
            className = "10А",
            dayOfWeek = "monday",
            lessonNumber = 3,
            subjectName = "Физика",
            teacherId = "teacher1",
            teacherName = "Петр Сидоров",
            room = "205",
            startTime = "10:30",
            endTime = "11:15"
        )

        val uiModel = ModelConverter.convertLessonToUIModel(lessonDTO)

        assertEquals("lesson1", uiModel.id)
        assertEquals(3, uiModel.lessonNumber)
        assertEquals("Физика", uiModel.subjectName)
        assertEquals("205", uiModel.room)
    }

    @Test
    fun testConvertUserToStudentModel() {
        val userDTO = UserDTO(
            userId = "student123",
            email = "student@school.com",
            name = "Анна",
            sName = "Смирнова",
            uClass = "10А",
            school = "Школа №1",
            role = "student"
        )

        val studentModel = ModelConverter.convertUserToStudentModel(userDTO)

        assertEquals("student123", studentModel.id)
        assertEquals("Анна Смирнова", studentModel.name)
        assertEquals("10А", studentModel.className)
    }

    @Test
    fun testConvertUserToStudentModelWithEmptyName() {
        val userDTO = UserDTO(
            userId = "student456",
            email = "test@school.com",
            name = "",
            sName = "",
            uClass = "9Б",
            school = "Школа",
            role = "student"
        )

        val studentModel = ModelConverter.convertUserToStudentModel(userDTO)

        assertEquals("", studentModel.name)
        assertEquals("9Б", studentModel.className)
    }

    @Test
    fun testValidateEmail_incorrectFormat() {
        fun isValidEmail(email: String): Boolean {
            return email.contains("@") &&
                    email.contains(".") &&
                    email.indexOf("@") > 0 &&
                    email.indexOf(".") > email.indexOf("@") + 1
        }

        assertFalse(isValidEmail("invalid-email"))
        assertFalse(isValidEmail("missing@dot"))
        assertFalse(isValidEmail("@missing.com"))
        assertFalse(isValidEmail(""))
    }
}