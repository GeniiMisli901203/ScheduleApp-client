// com.example.ks1compose.data.datasource.local.CacheManager.kt
package com.example.ks1compose.data.datasource.local

import android.content.Context
import com.example.ks1compose.data.DTOs.GradeDTO
import com.example.ks1compose.data.DTOs.LessonDTO
import com.example.ks1compose.data.DTOs.NewsDTO
import com.example.ks1compose.data.DTOs.ScheduleDTO
import com.example.ks1compose.domain.models.UserDTO

class CacheManager(private val context: Context) {

    private val appCache = AppCache(context)

    companion object {
        @Volatile
        private var instance: CacheManager? = null

        fun getInstance(context: Context): CacheManager {
            return instance ?: synchronized(this) {
                instance ?: CacheManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // ================ НОВОСТИ ================

    suspend fun getNews(forceRefresh: Boolean = false): List<NewsDTO>? {
        if (!forceRefresh && appCache.isNewsCacheValid()) {
            return appCache.getCachedNews()
        }
        return null
    }

    suspend fun saveNews(newsList: List<NewsDTO>) {
        appCache.cacheNews(newsList)
    }

    fun saveNewsAsync(newsList: List<NewsDTO>) {
        appCache.cacheNewsAsync(newsList)
    }

    // ================ РАСПИСАНИЕ ================

    suspend fun getSchedules(): List<ScheduleDTO>? {
        return appCache.getCachedSchedules()
    }

    suspend fun saveSchedules(schedules: List<ScheduleDTO>) {
        appCache.cacheSchedules(schedules)
    }

    // ================ УРОКИ ================

    suspend fun getLessons(className: String, dayOfWeek: String): List<LessonDTO>? {
        val key = appCache.getLessonsCacheKey(className, dayOfWeek)
        return appCache.getCachedLessons(key)
    }

    suspend fun saveLessons(className: String, dayOfWeek: String, lessons: List<LessonDTO>) {
        val key = appCache.getLessonsCacheKey(className, dayOfWeek)
        appCache.cacheLessons(lessons, key)
    }

    fun saveLessonsAsync(className: String, dayOfWeek: String, lessons: List<LessonDTO>) {
        val key = appCache.getLessonsCacheKey(className, dayOfWeek)
        appCache.cacheLessonsAsync(lessons, key)
    }

    // ================ ОЦЕНКИ ================

    suspend fun getGrades(studentId: String): List<GradeDTO>? {
        val key = appCache.getGradesCacheKey(studentId)
        return appCache.getCachedGrades(key)
    }

    suspend fun saveGrades(studentId: String, grades: List<GradeDTO>) {
        val key = appCache.getGradesCacheKey(studentId)
        appCache.cacheGrades(grades, key)
    }

    fun saveGradesAsync(studentId: String, grades: List<GradeDTO>) {
        val key = appCache.getGradesCacheKey(studentId)
        appCache.cacheGradesAsync(grades, key)
    }

    // ================ УЧИТЕЛЯ И УЧЕНИКИ ================

    suspend fun getTeachers(): List<UserDTO>? {
        return appCache.getCachedTeachers()
    }

    suspend fun saveTeachers(teachers: List<UserDTO>) {
        appCache.cacheTeachers(teachers)
    }

    suspend fun getStudents(): List<UserDTO>? {
        return appCache.getCachedStudents()
    }

    suspend fun saveStudents(students: List<UserDTO>) {
        appCache.cacheStudents(students)
    }

    // ================ ОЧИСТКА ================

    suspend fun clearAllCache() {
        appCache.clearAllCache()
    }

    suspend fun clearUserRelatedCache(userId: String) {
        // Исправлено: передаем строковые ключи, а не Preferences.Key
        appCache.clearCache(
            "grades_cache",  // Строковое представление GRADES_CACHE
            appCache.getGradesCacheKey(userId)
        )
    }
}