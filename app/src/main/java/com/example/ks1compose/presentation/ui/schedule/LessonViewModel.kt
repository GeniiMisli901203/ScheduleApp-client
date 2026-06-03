package com.example.ks1compose.presentation.ui.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.data.DTOs.LessonDTO
import com.example.ks1compose.data.datasource.remote.TokenManager
import com.example.ks1compose.data.repositories.LessonRepository
import com.example.ks1compose.domain.models.LessonUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class LessonViewModel(context: Context) : ViewModel() {

    private val _teacherLessons = MutableStateFlow<List<LessonUIModel>>(emptyList())
    val teacherLessons: StateFlow<List<LessonUIModel>> = _teacherLessons.asStateFlow()
    private val repository = LessonRepository(context)

    private val _todayLessons = MutableStateFlow<List<LessonUIModel>>(emptyList())
    val todayLessons: StateFlow<List<LessonUIModel>> = _todayLessons.asStateFlow()

    private val _weeklySchedule = MutableStateFlow<Map<String, List<LessonUIModel>>?>(null)
    val weeklySchedule: StateFlow<Map<String, List<LessonUIModel>>?> = _weeklySchedule.asStateFlow()

    private val _createLessonResult = MutableStateFlow<LessonRepository.Result<String>?>(null)
    val createLessonResult: StateFlow<LessonRepository.Result<String>?> = _createLessonResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Загрузка расписания на сегодня
    fun loadTodayLessons(className: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val dayOfWeek = getCurrentDayOfWeek()

            when (val result = repository.getLessonsByClassAndDay(className, dayOfWeek, null, forceRefresh)) {
                is LessonRepository.Result.Success -> {
                    _todayLessons.value = result.data
                }
                is LessonRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadTeacherLessons(teacherId: String, dayOfWeek: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getTeacherLessons(teacherId, dayOfWeek)) {
                is LessonRepository.Result.Success -> {
                    _teacherLessons.value = result.data
                }
                is LessonRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Загрузка расписания на конкретный день
    fun loadLessonsByDay(className: String, dayOfWeek: String, weekNumber: Int? = null, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getLessonsByClassAndDay(className, dayOfWeek, weekNumber, forceRefresh)) {
                is LessonRepository.Result.Success -> {
                    if (dayOfWeek == getCurrentDayOfWeek()) {
                        _todayLessons.value = result.data
                    }
                }
                is LessonRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Создание урока
    fun createLesson(
        className: String,
        dayOfWeek: String,
        lessonNumber: Int,
        subjectName: String,
        room: String? = null,
        startTime: String? = null,
        endTime: String? = null,
        weekNumber: Int? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val teacherId = TokenManager.userId ?: run {
                _error.value = "ID учителя не найден"
                _isLoading.value = false
                return@launch
            }

            val lesson = LessonDTO(
                className = className,
                dayOfWeek = dayOfWeek,
                weekNumber = weekNumber,
                lessonNumber = lessonNumber,
                subjectName = subjectName,
                teacherId = teacherId,
                room = room,
                startTime = startTime,
                endTime = endTime
            )

            _createLessonResult.value = repository.createLesson(lesson)
            _isLoading.value = false
        }
    }

    fun clearResults() {
        _createLessonResult.value = null
    }

    fun clearError() {
        _error.value = null
    }

    private fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "monday"
            Calendar.TUESDAY -> "tuesday"
            Calendar.WEDNESDAY -> "wednesday"
            Calendar.THURSDAY -> "thursday"
            Calendar.FRIDAY -> "friday"
            Calendar.SATURDAY -> "saturday"
            Calendar.SUNDAY -> "sunday"
            else -> "monday"
        }
    }

    companion object {
        val daysOfWeek = listOf(
            "monday" to "Понедельник",
            "tuesday" to "Вторник",
            "wednesday" to "Среда",
            "thursday" to "Четверг",
            "friday" to "Пятница",
            "saturday" to "Суббота",
            "sunday" to "Воскресенье"
        )

        fun getRussianDayName(englishDay: String): String {
            return daysOfWeek.find { it.first == englishDay }?.second ?: englishDay
        }
    }
}