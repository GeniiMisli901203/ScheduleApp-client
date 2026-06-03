package com.example.ks1compose.data.repositories

import android.content.Context
import com.example.ks1compose.data.DTOs.ScheduleDTO
import com.example.ks1compose.data.DTOs.ScheduleResponse
import com.example.ks1compose.data.datasource.remote.RetrofitInstance
import com.example.ks1compose.domain.models.RetrofitInstanceWithAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ScheduleRepository(private val context: Context) {
    private val api = RetrofitInstance.apiService
    private val apiWithAuth = RetrofitInstanceWithAuth.apiService
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }


    suspend fun getAllSchedules(): Response<ScheduleResponse> {
        return try {
            api.getAllSchedules()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAllSchedulesCached(): Result<List<ScheduleDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAllSchedules()
                if (response.isSuccessful && response.body() != null) {
                    val schedules = response.body()!!.schedules ?: emptyList()
                    saveSchedulesToCache(schedules)
                    Result.Success(schedules)
                } else {
                    getSchedulesFromCache()?.let {
                        Result.Success(it)
                    } ?: Result.Error("Нет данных и нет кэша")
                }
            } catch (e: HttpException) {
                getSchedulesFromCache()?.let {
                    Result.Success(it)
                } ?: Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                getSchedulesFromCache()?.let {
                    Result.Success(it)
                } ?: Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                getSchedulesFromCache()?.let {
                    Result.Success(it)
                } ?: Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun getSchedulesByDay(day: String): Response<ScheduleResponse> {
        return try {
            api.getSchedulesByDay(day)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getSchedulesByDayCached(day: String): Result<List<ScheduleDTO>> {
        val cacheKey = "schedule_day_${day}"

        return withContext(Dispatchers.IO) {
            try {
                val response = api.getSchedulesByDay(day)
                if (response.isSuccessful && response.body() != null) {
                    val schedules = response.body()!!.schedules ?: emptyList()
                    saveScheduleToCache(cacheKey, schedules)
                    Result.Success(schedules)
                } else {
                    getScheduleFromCache(cacheKey)?.let {
                        Result.Success(it)
                    } ?: Result.Error("Расписание не найдено")
                }
            } catch (e: HttpException) {
                getScheduleFromCache(cacheKey)?.let {
                    Result.Success(it)
                } ?: Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                getScheduleFromCache(cacheKey)?.let {
                    Result.Success(it)
                } ?: Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                getScheduleFromCache(cacheKey)?.let {
                    Result.Success(it)
                } ?: Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun getSchedule(className: String, day: String): Response<ScheduleResponse> {
        return try {
            api.getSchedule(className, day)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getScheduleCached(className: String, day: String): Result<List<ScheduleDTO>> {
        val cacheKey = "schedule_${className}_${day}"

        return withContext(Dispatchers.IO) {
            try {
                val response = api.getSchedule(className, day)
                if (response.isSuccessful && response.body() != null) {
                    val schedules = response.body()!!.schedules ?: emptyList()
                    saveScheduleToCache(cacheKey, schedules)
                    Result.Success(schedules)
                } else {
                    getScheduleFromCache(cacheKey)?.let {
                        Result.Success(it)
                    } ?: Result.Error("Расписание не найдено")
                }
            } catch (e: HttpException) {
                getScheduleFromCache(cacheKey)?.let {
                    Result.Success(it)
                } ?: Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                getScheduleFromCache(cacheKey)?.let {
                    Result.Success(it)
                } ?: Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                getScheduleFromCache(cacheKey)?.let {
                    Result.Success(it)
                } ?: Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun addSchedule(token: String, schedule: ScheduleDTO): Response<ScheduleResponse> {
        return try {
            val response = apiWithAuth.addSchedule(token, schedule)
            if (response.isSuccessful) {
                clearScheduleCache()
            }
            response
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteSchedule(token: String, scheduleId: String): Response<ScheduleResponse> {
        return try {
            val response = apiWithAuth.deleteSchedule(token, scheduleId)
            if (response.isSuccessful) {
                clearScheduleCache()
            }
            response
        } catch (e: Exception) {
            throw e
        }
    }


    private fun saveSchedulesToCache(schedules: List<ScheduleDTO>) {
        try {
            val json = gson.toJson(schedules)
            prefs.edit().putString("cached_schedules", json).apply()
            prefs.edit().putLong("cache_time", System.currentTimeMillis()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSchedulesFromCache(): List<ScheduleDTO>? {
        return try {
            val json = prefs.getString("cached_schedules", null)
            val cacheTime = prefs.getLong("cache_time", 0)
            val isExpired = System.currentTimeMillis() - cacheTime > 30 * 60 * 1000 // 30 минут

            if (json != null && !isExpired) {
                val type = object : TypeToken<List<ScheduleDTO>>() {}.type
                gson.fromJson<List<ScheduleDTO>>(json, type)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveScheduleToCache(key: String, schedules: List<ScheduleDTO>) {
        try {
            val json = gson.toJson(schedules)
            prefs.edit().putString(key, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getScheduleFromCache(key: String): List<ScheduleDTO>? {
        return try {
            val json = prefs.getString(key, null)
            if (json != null) {
                val type = object : TypeToken<List<ScheduleDTO>>() {}.type
                gson.fromJson<List<ScheduleDTO>>(json, type)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun clearScheduleCache() {
        prefs.edit().clear().apply()
    }
}