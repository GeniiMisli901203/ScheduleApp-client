package com.example.ks1compose.data.repositories

import android.content.Context
import com.example.ks1compose.data.DTOs.LessonDTO
import com.example.ks1compose.data.datasource.local.CacheManager
import com.example.ks1compose.domain.models.LessonUIModel
import com.example.ks1compose.domain.models.ModelConverter
import com.example.ks1compose.data.datasource.remote.RetrofitInstance
import com.example.ks1compose.data.datasource.remote.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class LessonRepository(private val context: Context) {
    private val api = RetrofitInstance.apiService
    private val cacheManager = CacheManager.getInstance(context)

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    suspend fun getLessonsByClassAndDay(
        className: String,
        dayOfWeek: String,
        weekNumber: Int? = null,
        forceRefresh: Boolean = false
    ): Result<List<LessonUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                // Пробуем получить из кэша
                if (!forceRefresh) {
                    val cachedLessons = cacheManager.getLessons(className, dayOfWeek)
                    if (cachedLessons != null) {
                        println("📦 Загружено из кэша: ${cachedLessons.size} уроков для $className $dayOfWeek")
                        val uiModels = cachedLessons.map { ModelConverter.convertLessonToUIModel(it) }
                        return@withContext Result.Success(uiModels)
                    }
                }

                val response = api.getLessonsByClassAndDay(className, dayOfWeek, weekNumber)

                if (response.isSuccessful && response.body() != null) {
                    val lessons = response.body()!!.lessons ?: emptyList()


                    cacheManager.saveLessons(className, dayOfWeek, lessons)

                    val uiModels = lessons.map { lesson ->
                        ModelConverter.convertLessonToUIModel(lesson)
                    }
                    println("📡 Загружено с сервера: ${uiModels.size} уроков")
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка получения расписания", response.code())
                }
            } catch (e: HttpException) {
                val cachedLessons = cacheManager.getLessons(className, dayOfWeek)
                if (cachedLessons != null) {
                    val uiModels = cachedLessons.map { ModelConverter.convertLessonToUIModel(it) }
                    Result.Success(uiModels)
                } else {
                    Result.Error("Ошибка сети: ${e.message()}", e.code())
                }
            } catch (e: IOException) {
                val cachedLessons = cacheManager.getLessons(className, dayOfWeek)
                if (cachedLessons != null) {
                    val uiModels = cachedLessons.map { ModelConverter.convertLessonToUIModel(it) }
                    Result.Success(uiModels)
                } else {
                    Result.Error("Проверьте подключение к интернету")
                }
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun createLesson(lesson: LessonDTO): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.createLesson("Bearer $token", lesson)

                if (response.isSuccessful) {
                    // Очищаем кэш для этого класса
                    cacheManager.clearAllCache()
                    Result.Success("Урок успешно создан")
                } else {
                    Result.Error(response.message() ?: "Ошибка создания урока", response.code())
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun updateLesson(lessonId: String, updates: Map<String, Any?>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.updateLesson("Bearer $token", lessonId, updates)

                if (response.isSuccessful) {
                    cacheManager.clearAllCache()
                    Result.Success("Урок обновлен")
                } else {
                    Result.Error(response.message() ?: "Ошибка обновления урока", response.code())
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }


    suspend fun getTeacherLessons(teacherId: String, dayOfWeek: String? = null): Result<List<LessonUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getTeacherLessons(teacherId, dayOfWeek, null)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val lessonsJson = data["lessons"] as? List<Map<String, Any>> ?: emptyList()

                    val lessons = lessonsJson.map { lessonMap ->
                        LessonDTO(
                            lessonId = lessonMap["lessonId"] as? String,
                            className = lessonMap["className"] as? String ?: "",
                            dayOfWeek = lessonMap["dayOfWeek"] as? String ?: "",
                            weekNumber = lessonMap["weekNumber"] as? Int,
                            lessonNumber = (lessonMap["lessonNumber"] as? Double)?.toInt() ?: 0,
                            subjectName = lessonMap["subjectName"] as? String ?: "",
                            teacherId = lessonMap["teacherId"] as? String,
                            room = lessonMap["room"] as? String,
                            startTime = lessonMap["startTime"] as? String,
                            endTime = lessonMap["endTime"] as? String
                        )
                    }

                    val uiModels = lessons.map { ModelConverter.convertLessonToUIModel(it) }
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки расписания", response.code())
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun deleteLesson(lessonId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.deleteLesson("Bearer $token", lessonId)

                if (response.isSuccessful) {
                    cacheManager.clearAllCache()
                    Result.Success("Урок удален")
                } else {
                    Result.Error(response.message() ?: "Ошибка удаления урока", response.code())
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }
}