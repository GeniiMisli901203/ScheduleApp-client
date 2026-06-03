
package com.example.ks1compose.data.repositories

import android.content.Context
import com.example.ks1compose.data.DTOs.GradeDTO
import com.example.ks1compose.data.DTOs.UpdateGradeRequest
import com.example.ks1compose.data.datasource.local.CacheManager
import com.example.ks1compose.domain.models.GradeUIModel
import com.example.ks1compose.domain.models.ModelConverter
import com.example.ks1compose.domain.models.RetrofitInstanceWithAuth
import com.example.ks1compose.data.datasource.remote.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class GradeRepository(private val context: Context) {
    private val api = RetrofitInstanceWithAuth.apiService
    private val cacheManager = CacheManager.getInstance(context)

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    suspend fun getTodayGrades(studentId: String): Result<List<GradeUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.getTodayGrades(studentId, "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val grades = response.body()!!.grades ?: emptyList()
                    val uiModels = grades.map { ModelConverter.convertGradeToUIModel(it) }
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки", response.code())
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

    suspend fun getMyGrades(
        subject: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        forceRefresh: Boolean = false
    ): Result<List<GradeUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val studentId = TokenManager.userId ?: return@withContext Result.Error("Не авторизован")


                if (!forceRefresh) {
                    val cachedGrades = cacheManager.getGrades(studentId)
                    if (cachedGrades != null) {
                        println("📦 Загружено из кэша: ${cachedGrades.size} оценок")
                        val uiModels = cachedGrades.map { ModelConverter.convertGradeToUIModel(it) }
                        return@withContext Result.Success(uiModels)
                    }
                }

                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.getMyGrades("Bearer $token", subject, startDate, endDate)

                if (response.isSuccessful && response.body() != null) {
                    val grades = response.body()!!.grades ?: emptyList()
                    val gradeDTOs = grades

                    cacheManager.saveGrades(studentId, gradeDTOs)

                    val uiModels = grades.map { ModelConverter.convertGradeToUIModel(it) }
                    println("📡 Загружено с сервера: ${uiModels.size} оценок")
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки", response.code())
                }
            } catch (e: HttpException) {
                val studentId = TokenManager.userId
                val cachedGrades = studentId?.let { cacheManager.getGrades(it) }
                if (cachedGrades != null) {
                    val uiModels = cachedGrades.map { ModelConverter.convertGradeToUIModel(it) }
                    Result.Success(uiModels)
                } else {
                    Result.Error("Ошибка сети: ${e.message()}", e.code())
                }
            } catch (e: IOException) {
                val studentId = TokenManager.userId
                val cachedGrades = studentId?.let { cacheManager.getGrades(it) }
                if (cachedGrades != null) {
                    val uiModels = cachedGrades.map { ModelConverter.convertGradeToUIModel(it) }
                    Result.Success(uiModels)
                } else {
                    Result.Error("Проверьте подключение к интернету")
                }
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }


    suspend fun getUserGrades(userId: String): Result<List<GradeUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.getUserGrades(userId, "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val gradeResponse = response.body()!!
                    val grades = gradeResponse.grades ?: emptyList()
                    val uiModels = grades.map { grade ->
                        ModelConverter.convertGradeToUIModel(grade)
                    }
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки", response.code())
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

    // Получить оценки класса
    suspend fun getClassGrades(className: String, subject: String? = null): Result<List<GradeUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.getClassGrades("Bearer $token", className, subject)

                if (response.isSuccessful && response.body() != null) {
                    val grades = response.body()!!.grades ?: emptyList()
                    val uiModels = grades.map { ModelConverter.convertGradeToUIModel(it) }
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки", response.code())
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

    // Добавить оценку
    suspend fun addGrade(grade: GradeDTO): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.addGrade("Bearer $token", grade)

                if (response.isSuccessful) {
                    // Очищаем кэш оценок для этого ученика
                    cacheManager.clearUserRelatedCache(grade.studentId)
                    Result.Success("Оценка успешно добавлена")
                } else {
                    Result.Error(response.message() ?: "Ошибка добавления", response.code())
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

    // Обновить оценку
    suspend fun updateGrade(gradeId: String, gradeValue: Int, comment: String?): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val request = UpdateGradeRequest(gradeValue, comment)
                val response = api.updateGrade("Bearer $token", gradeId, request)

                if (response.isSuccessful) {
                    // Очищаем кэш (в реальном приложении нужно знать studentId)
                    // Для простоты очищаем все кэши
                    cacheManager.clearAllCache()
                    Result.Success("Оценка обновлена")
                } else {
                    Result.Error(response.message() ?: "Ошибка обновления", response.code())
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

    // Удалить оценку
    suspend fun deleteGrade(gradeId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.deleteGrade("Bearer $token", gradeId)

                if (response.isSuccessful) {
                    // Очищаем все кэши
                    cacheManager.clearAllCache()
                    Result.Success("Оценка удалена")
                } else {
                    Result.Error(response.message() ?: "Ошибка удаления", response.code())
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