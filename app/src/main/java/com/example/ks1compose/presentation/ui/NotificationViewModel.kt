// com.example.ks1compose.presentation.ui.NotificationViewModel.kt
package com.example.ks1compose.presentation.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.domain.models.RetrofitInstanceWithAuth
import com.example.ks1compose.data.datasource.remote.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class NotificationUIModel(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
    val createdAt: String
)

class NotificationViewModel(context: Context) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationUIModel>>(emptyList())
    val notifications: StateFlow<List<NotificationUIModel>> = _notifications.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var eventSource: EventSource? = null

    init {
        connectToSse()
    }

    private fun connectToSse() {
        val token = TokenManager.authToken ?: return

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // Бесконечный таймаут для SSE
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2:8080/notifications/stream")
            .header("Authorization", "Bearer $token")
            .build()

        val factory = EventSources.createFactory(client)
        eventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                viewModelScope.launch {
                    _isConnected.value = true
                    println("📱 SSE Connected")
                }
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                viewModelScope.launch {
                    when (type) {
                        "notification" -> {
                            try {
                                val json = JSONObject(data)
                                val notification = NotificationUIModel(
                                    id = json.optString("id"),
                                    type = json.optString("type"),
                                    title = json.optString("title"),
                                    body = json.optString("body"),
                                    data = parseData(json.optJSONObject("data")),
                                    createdAt = json.optString("createdAt")
                                )

                                val currentList = _notifications.value.toMutableList()
                                currentList.add(0, notification)
                                _notifications.value = currentList.take(50) // Храним последние 50

                                println("📱 New notification: ${notification.title}")
                            } catch (e: Exception) {
                                println("❌ Error parsing notification: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                        "connected" -> {
                            _isConnected.value = true
                            println("📱 SSE Connected (event)")
                        }
                        "error" -> {
                            _isConnected.value = false
                            println("📱 SSE Error: $data")
                        }
                        else -> {
                            println("📱 SSE Unknown event: $type")
                        }
                    }
                }
            }

            override fun onClosed(eventSource: EventSource) {
                viewModelScope.launch {
                    _isConnected.value = false
                    println("📱 SSE Closed")
                    // Пытаемся переподключиться через 5 секунд
                    delay(5000)
                    connectToSse()
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                viewModelScope.launch {
                    _isConnected.value = false
                    println("📱 SSE Failure: ${t?.message}")
                    // Пытаемся переподключиться через 10 секунд
                    delay(10000)
                    connectToSse()
                }
            }
        })
    }

    private fun parseData(jsonObject: JSONObject?): Map<String, String> {
        val result = mutableMapOf<String, String>()
        if (jsonObject != null) {
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                result[key] = jsonObject.optString(key)
            }
        }
        return result
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                val token = TokenManager.authToken ?: return@launch
                val response = RetrofitInstanceWithAuth.apiService.getNotificationsHistory("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val success = data["success"] as? Boolean ?: false
                    if (success) {
                        val notificationsList = data["notifications"] as? List<Map<String, Any>> ?: emptyList()
                        val uiModels = notificationsList.mapNotNull { notif ->
                            try {
                                NotificationUIModel(
                                    id = notif["id"] as? String ?: return@mapNotNull null,
                                    type = notif["type"] as? String ?: "",
                                    title = notif["title"] as? String ?: "",
                                    body = notif["body"] as? String ?: "",
                                    data = notif["data"] as? Map<String, String> ?: emptyMap(),
                                    createdAt = notif["createdAt"] as? String ?: ""
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _notifications.value = uiModels
                    }
                }
            } catch (e: Exception) {
                println("❌ Error loading history: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        eventSource?.cancel()
    }
}