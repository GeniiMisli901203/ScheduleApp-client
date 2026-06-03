
package com.example.ks1compose.data.repositories

import android.content.Context
import com.example.ks1compose.data.datasource.local.CacheManager
import com.example.ks1compose.domain.models.StudentUIModel
import com.example.ks1compose.data.datasource.remote.RetrofitInstance
import com.example.ks1compose.domain.models.RetrofitInstanceWithAuth
import com.example.ks1compose.data.datasource.remote.TokenManager
import com.example.ks1compose.domain.models.UpdateUserRequest
import com.example.ks1compose.domain.models.UserDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class UserRepository(private val context: Context) {
    private val api = RetrofitInstance.apiService
    private val apiWithAuth = RetrofitInstanceWithAuth.apiService
    private val cacheManager = CacheManager.getInstance(context)

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    suspend fun getUserInfo(): Result<UserDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.getUserInfoByToken("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.user
                    if (user != null) {
                        Result.Success(user)
                    } else {
                        Result.Error("Пользователь не найден")
                    }
                } else {
                    Result.Error(response.message() ?: "Ошибка получения информации")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    suspend fun getUserById(userId: String): Result<UserDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getUserById(userId)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.user
                    if (user != null) {
                        Result.Success(user)
                    } else {
                        Result.Error("Пользователь не найден")
                    }
                } else {
                    Result.Error(response.message() ?: "Ошибка получения пользователя")
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}")
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun updateUserInfo(
        userId: String,
        name: String,
        sName: String,
        uClass: String,
        school: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val request = UpdateUserRequest(name, sName, uClass, school)

                println("📤 Sending update for user $userId")

                val currentUser = getUserInfo().getOrNull()
                val isAdmin = currentUser?.role == "admin"

                val response = if (isAdmin && currentUser?.userId != userId) {

                    println("📤 Admin updating user $userId")
                    api.updateUserById(userId, "Bearer $token", request)
                } else {

                    println("📤 User updating own profile")
                    api.updateUserInfo("Bearer $token", request)
                }

                if (response.isSuccessful) {
                    println("✅ Update successful: ${response.body()}")
                    Result.Success("Данные обновлены")
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("❌ Update failed: ${response.code()} - ${response.message()}")
                    println("❌ Error body: $errorBody")
                    Result.Error(response.message() ?: "Ошибка обновления")
                }
            } catch (e: Exception) {
                println("🔥 Exception: ${e.message}")
                e.printStackTrace()
                Result.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }


    private fun <T> Result<T>.getOrNull(): T? {
        return if (this is Result.Success) this.data else null
    }

    suspend fun getStudentsByClass(className: String): Result<List<StudentUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getStudentsByClass(className)

                if (response.isSuccessful && response.body() != null) {
                    val students = response.body()!!.students ?: emptyList()
                    val uiModels = students.map { user ->
                        StudentUIModel(
                            id = user.userId,
                            name = "${user.name} ${user.sName}",
                            className = user.uClass
                        )
                    }
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки учеников")
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}")
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun getAllTeachers(forceRefresh: Boolean = false): Result<List<UserDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                // Пробуем получить из кэша
                if (!forceRefresh) {
                    val cachedTeachers = cacheManager.getTeachers()
                    if (cachedTeachers != null) {
                        println("📦 Загружено из кэша: ${cachedTeachers.size} учителей")
                        return@withContext Result.Success(cachedTeachers)
                    }
                }

                val response = api.getAllTeachers()
                if (response.isSuccessful && response.body() != null) {
                    val teachers = response.body()!!.teachers ?: emptyList()
                    // Сохраняем в кэш
                    cacheManager.saveTeachers(teachers)
                    println("📡 Загружено с сервера: ${teachers.size} учителей")
                    Result.Success(teachers)
                } else {
                    Result.Error(response.message() ?: "Ошибка получения учителей")
                }
            } catch (e: HttpException) {
                val cachedTeachers = cacheManager.getTeachers()
                if (cachedTeachers != null) {
                    Result.Success(cachedTeachers)
                } else {
                    Result.Error("Ошибка сети: ${e.message()}")
                }
            } catch (e: IOException) {
                val cachedTeachers = cacheManager.getTeachers()
                if (cachedTeachers != null) {
                    Result.Success(cachedTeachers)
                } else {
                    Result.Error("Проверьте подключение к интернету")
                }
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun getAllStudents(forceRefresh: Boolean = false): Result<List<UserDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!forceRefresh) {
                    val cachedStudents = cacheManager.getStudents()
                    if (cachedStudents != null) {
                        println("📦 Загружено из кэша: ${cachedStudents.size} учеников")
                        return@withContext Result.Success(cachedStudents)
                    }
                }

                val response = api.getAllStudents()
                if (response.isSuccessful && response.body() != null) {
                    val students = response.body()!!.students ?: emptyList()
                    cacheManager.saveStudents(students)
                    println("📡 Загружено с сервера: ${students.size} учеников")
                    Result.Success(students)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки учеников")
                }
            } catch (e: HttpException) {
                val cachedStudents = cacheManager.getStudents()
                if (cachedStudents != null) {
                    Result.Success(cachedStudents)
                } else {
                    Result.Error("Ошибка сети: ${e.message()}")
                }
            } catch (e: IOException) {
                val cachedStudents = cacheManager.getStudents()
                if (cachedStudents != null) {
                    Result.Success(cachedStudents)
                } else {
                    Result.Error("Проверьте подключение к интернету")
                }
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }
}