// com.example.ks1compose.data.datasource.local.AppCache.kt
package com.example.ks1compose.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ks1compose.data.DTOs.GradeDTO
import com.example.ks1compose.data.DTOs.LessonDTO
import com.example.ks1compose.data.DTOs.NewsDTO
import com.example.ks1compose.data.DTOs.ScheduleDTO
import com.example.ks1compose.domain.models.UserDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.cacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_cache")

class AppCache(private val context: Context) {

    private val gson = Gson()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    companion object {

        val NEWS_CACHE = stringPreferencesKey("news_cache")
        val NEWS_TIMESTAMP = stringPreferencesKey("news_timestamp")

        val SCHEDULE_CACHE = stringPreferencesKey("schedule_cache")
        val SCHEDULE_TIMESTAMP = stringPreferencesKey("schedule_timestamp")

        val LESSONS_CACHE = stringPreferencesKey("lessons_cache")
        val LESSONS_TIMESTAMP = stringPreferencesKey("lessons_timestamp")

        val GRADES_CACHE = stringPreferencesKey("grades_cache")
        val GRADES_TIMESTAMP = stringPreferencesKey("grades_timestamp")

        val TEACHERS_CACHE = stringPreferencesKey("teachers_cache")
        val STUDENTS_CACHE = stringPreferencesKey("students_cache")
    }


    suspend fun cacheNews(newsList: List<NewsDTO>) {
        val json = gson.toJson(newsList)
        context.cacheDataStore.edit { preferences ->
            preferences[NEWS_CACHE] = json
            preferences[NEWS_TIMESTAMP] = System.currentTimeMillis().toString()
        }
    }

    suspend fun getCachedNews(): List<NewsDTO>? {
        val preferences = context.cacheDataStore.data.first()
        val json = preferences[NEWS_CACHE] ?: return null
        val type = object : TypeToken<List<NewsDTO>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun isNewsCacheValid(maxAgeMs: Long = 30 * 60 * 1000): Boolean {
        val preferences = context.cacheDataStore.data.first()
        val timestamp = preferences[NEWS_TIMESTAMP]?.toLongOrNull() ?: return false
        return System.currentTimeMillis() - timestamp < maxAgeMs
    }



    suspend fun cacheSchedules(schedules: List<ScheduleDTO>) {
        val json = gson.toJson(schedules)
        context.cacheDataStore.edit { preferences ->
            preferences[SCHEDULE_CACHE] = json
            preferences[SCHEDULE_TIMESTAMP] = System.currentTimeMillis().toString()
        }
    }

    suspend fun getCachedSchedules(): List<ScheduleDTO>? {
        val preferences = context.cacheDataStore.data.first()
        val json = preferences[SCHEDULE_CACHE] ?: return null
        val type = object : TypeToken<List<ScheduleDTO>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }



    suspend fun cacheLessons(lessons: List<LessonDTO>, key: String) {
        val json = gson.toJson(lessons)
        val prefKey = stringPreferencesKey(key)
        context.cacheDataStore.edit { preferences ->
            preferences[prefKey] = json
            preferences[LESSONS_TIMESTAMP] = System.currentTimeMillis().toString()
        }
    }

    suspend fun getCachedLessons(key: String): List<LessonDTO>? {
        val preferences = context.cacheDataStore.data.first()
        val prefKey = stringPreferencesKey(key)
        val json = preferences[prefKey] ?: return null
        val type = object : TypeToken<List<LessonDTO>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun getLessonsCacheKey(className: String, dayOfWeek: String): String {
        return "lessons_${className}_${dayOfWeek}"
    }


    suspend fun cacheGrades(grades: List<GradeDTO>, key: String) {
        val json = gson.toJson(grades)
        val prefKey = stringPreferencesKey(key)
        context.cacheDataStore.edit { preferences ->
            preferences[prefKey] = json
            preferences[GRADES_TIMESTAMP] = System.currentTimeMillis().toString()
        }
    }

    suspend fun getCachedGrades(key: String): List<GradeDTO>? {
        val preferences = context.cacheDataStore.data.first()
        val prefKey = stringPreferencesKey(key)
        val json = preferences[prefKey] ?: return null
        val type = object : TypeToken<List<GradeDTO>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun getGradesCacheKey(studentId: String): String {
        return "grades_${studentId}"
    }


    suspend fun cacheTeachers(teachers: List<UserDTO>) {
        val json = gson.toJson(teachers)
        context.cacheDataStore.edit { preferences ->
            preferences[TEACHERS_CACHE] = json
        }
    }

    suspend fun getCachedTeachers(): List<UserDTO>? {
        val preferences = context.cacheDataStore.data.first()
        val json = preferences[TEACHERS_CACHE] ?: return null
        val type = object : TypeToken<List<UserDTO>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun cacheStudents(students: List<UserDTO>) {
        val json = gson.toJson(students)
        context.cacheDataStore.edit { preferences ->
            preferences[STUDENTS_CACHE] = json
        }
    }

    suspend fun getCachedStudents(): List<UserDTO>? {
        val preferences = context.cacheDataStore.data.first()
        val json = preferences[STUDENTS_CACHE] ?: return null
        val type = object : TypeToken<List<UserDTO>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }


    suspend fun clearAllCache() {
        context.cacheDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun clearCache(vararg keys: String) {
        context.cacheDataStore.edit { preferences ->
            keys.forEach { key ->
                val prefKey = stringPreferencesKey(key)
                preferences.remove(prefKey)
            }
        }
    }

    fun cacheNewsAsync(newsList: List<NewsDTO>) {
        ioScope.launch {
            cacheNews(newsList)
        }
    }

    fun cacheLessonsAsync(lessons: List<LessonDTO>, key: String) {
        ioScope.launch {
            cacheLessons(lessons, key)
        }
    }

    fun cacheGradesAsync(grades: List<GradeDTO>, key: String) {
        ioScope.launch {
            cacheGrades(grades, key)
        }
    }
}