package com.example.ks1compose.presentation.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.data.DTOs.ScheduleDTO
import com.example.ks1compose.data.DTOs.ScheduleResponse
import com.example.ks1compose.data.repositories.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URLEncoder

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScheduleRepository(application.applicationContext)

    private val _scheduleResponse = MutableStateFlow<ScheduleResponse?>(null)
    val scheduleResponse: StateFlow<ScheduleResponse?> = _scheduleResponse.asStateFlow()

    private val _allSchedules = MutableStateFlow<List<ScheduleDTO>>(emptyList())
    val allSchedules: StateFlow<List<ScheduleDTO>> = _allSchedules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()

    fun loadAllSchedules() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = repository.getAllSchedules()
                if (response.isSuccessful) {
                    _allSchedules.value = response.body()?.schedules ?: emptyList()
                    _scheduleResponse.value = response.body()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSchedule(token: String, schedule: ScheduleDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _operationResult.value = null

            try {
                val response = repository.addSchedule(token, schedule)
                if (response.isSuccessful) {
                    _operationResult.value = "Расписание успешно добавлено"
                    loadAllSchedules()
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSchedule(className: String, day: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val encodedClassName = URLEncoder.encode(className, "UTF-8")
                val encodedDay = URLEncoder.encode(day, "UTF-8")
                val response = repository.getSchedule(encodedClassName, encodedDay)

                if (response.isSuccessful) {
                    _allSchedules.value = response.body()?.schedules ?: emptyList()
                    _scheduleResponse.value = response.body()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSchedulesByDay(day: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val encodedDay = URLEncoder.encode(day, "UTF-8")
                val response = repository.getSchedulesByDay(encodedDay)

                if (response.isSuccessful) {
                    _allSchedules.value = response.body()?.schedules ?: emptyList()
                    _scheduleResponse.value = response.body()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSchedule(token: String, scheduleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _operationResult.value = null

            try {
                val response = repository.deleteSchedule(token, scheduleId)
                if (response.isSuccessful) {
                    _operationResult.value = "Расписание удалено"
                    loadAllSchedules()
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    fun clearMessages() {
        _errorMessage.value = null
        _operationResult.value = null
    }
}