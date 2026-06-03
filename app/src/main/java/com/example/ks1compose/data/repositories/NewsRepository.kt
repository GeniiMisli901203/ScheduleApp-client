// com.example.ks1compose.data.repositories.NewsRepository.kt
package com.example.ks1compose.data.repositories

import android.content.Context
import com.example.ks1compose.data.DTOs.NewsDTO
import com.example.ks1compose.data.DTOs.NewsResponse
import com.example.ks1compose.data.datasource.local.CacheManager
import com.example.ks1compose.data.datasource.remote.RetrofitInstance
import com.example.ks1compose.domain.models.RetrofitInstanceWithAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NewsRepository(private val context: Context) {
    private val api = RetrofitInstance.apiService
    private val apiWithAuth = RetrofitInstanceWithAuth.apiService
    private val cacheManager = CacheManager.getInstance(context)

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    // ================ ОСНОВНЫЕ МЕТОДЫ ================

    // Получить все новости с поддержкой кэша
    suspend fun getAllNews(forceRefresh: Boolean = false): Result<List<NewsDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                // Пробуем получить из кэша
                if (!forceRefresh) {
                    val cachedNews = cacheManager.getNews()
                    if (cachedNews != null) {
                        println("📦 Загружено из кэша: ${cachedNews.size} новостей")
                        return@withContext Result.Success(cachedNews)
                    }
                }

                // Загружаем с сервера
                val response = api.getAllNews()
                if (response.isSuccessful && response.body() != null) {
                    val newsList = response.body()!!.newsList ?: emptyList()
                    // Сохраняем в кэш
                    cacheManager.saveNews(newsList)
                    println("📡 Загружено с сервера: ${newsList.size} новостей")
                    Result.Success(newsList)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки", response.code())
                }
            } catch (e: HttpException) {
                // При ошибке сети пробуем кэш
                val cachedNews = cacheManager.getNews()
                if (cachedNews != null) {
                    Result.Success(cachedNews)
                } else {
                    Result.Error("Ошибка сети: ${e.message()}", e.code())
                }
            } catch (e: IOException) {
                val cachedNews = cacheManager.getNews()
                if (cachedNews != null) {
                    Result.Success(cachedNews)
                } else {
                    Result.Error("Проверьте подключение к интернету")
                }
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    // Добавить новость
    suspend fun addNews(token: String, news: NewsDTO): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiWithAuth.addNews(token, news)
                if (response.isSuccessful && response.body() != null) {
                    // Очищаем кэш при добавлении
                    cacheManager.clearAllCache()
                    Result.Success("Новость успешно добавлена")
                } else {
                    Result.Error(
                        response.message() ?: "Ошибка добавления новости",
                        response.code()
                    )
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

    // Поиск новостей
    suspend fun searchNews(query: String): Response<NewsResponse> {
        return try {
            api.searchNews(query)
        } catch (e: Exception) {
            throw e
        }
    }

    // Удалить новость
    suspend fun deleteNews(token: String, newsId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiWithAuth.deleteNews(token, newsId)
                if (response.isSuccessful) {
                    // Очищаем кэш при удалении
                    cacheManager.clearAllCache()
                    Result.Success("Новость успешно удалена")
                } else {
                    Result.Error(
                        response.message() ?: "Ошибка удаления новости",
                        response.code()
                    )
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