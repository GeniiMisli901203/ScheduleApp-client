// com.example.ks1compose.presentation.ui.news.NewsViewModel.kt
package com.example.ks1compose.presentation.ui.news

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.data.DTOs.NewsDTO
import com.example.ks1compose.data.DTOs.NewsResponse
import com.example.ks1compose.data.datasource.remote.TokenManager
import com.example.ks1compose.data.repositories.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class NewsViewModel(context: Context) : ViewModel() {
    private val repository = NewsRepository(context)

    private val _allNews = MutableStateFlow<List<NewsDTO>>(emptyList())
    val allNews: StateFlow<List<NewsDTO>> = _allNews.asStateFlow()

    private val _newsResponse = MutableStateFlow<NewsResponse?>(null)
    val newsResponse: StateFlow<NewsResponse?> = _newsResponse.asStateFlow()

    private val _addNewsResult = MutableStateFlow<String?>(null)
    val addNewsResult: StateFlow<String?> = _addNewsResult.asStateFlow()

    private val _deleteNewsResult = MutableStateFlow<String?>(null)
    val deleteNewsResult: StateFlow<String?> = _deleteNewsResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ИСПРАВЛЕНО: используем _error вместо _errorMessage
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAllNews()
    }

    fun loadAllNews(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getAllNews(forceRefresh)) {
                is NewsRepository.Result.Success -> {
                    _allNews.value = result.data
                    _error.value = null
                }
                is NewsRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun addNews(token: String, title: String, description: String, url: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val userId = TokenManager.userId ?: run {
                _error.value = "Не авторизован"
                _isLoading.value = false
                return@launch
            }

            val news = NewsDTO(
                userId = userId,
                title = title,
                description = description,
                url = url
            )

            when (val result = repository.addNews(token, news)) {
                is NewsRepository.Result.Success -> {
                    _addNewsResult.value = result.data
                    loadAllNews(true)
                }
                is NewsRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun searchNews(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = repository.searchNews(query)
                if (response.isSuccessful) {
                    _allNews.value = response.body()?.newsList ?: emptyList()
                    _newsResponse.value = response.body()
                } else {
                    _error.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _error.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNews(token: String, newsId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.deleteNews(token, newsId)) {
                is NewsRepository.Result.Success -> {
                    _deleteNewsResult.value = result.data
                    loadAllNews(true)
                }
                is NewsRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearResults() {
        _addNewsResult.value = null
        _deleteNewsResult.value = null
        _error.value = null
    }
}