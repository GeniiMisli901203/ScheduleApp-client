// com.example.ks1compose.services.FCMService.kt
package com.example.ks1compose.data.datasource.services

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ks1compose.MainActivity
import com.example.ks1compose.domain.models.FcmTokenRequest
import com.example.ks1compose.domain.models.RetrofitInstanceWithAuth
import com.example.ks1compose.data.datasource.remote.TokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "school_notifications"
        private const val CHANNEL_NAME = "Школьные уведомления"
        private var fcmToken: String? = null

        fun getToken(): String? = fcmToken

        fun sendTokenToServer(context: Context) {
            val token = fcmToken ?: return
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val api = RetrofitInstanceWithAuth.apiService
                    val authToken = TokenManager.authToken
                    if (authToken != null) {
                        api.registerFcmToken("Bearer $authToken", FcmTokenRequest(token))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        fcmToken = token
        sendTokenToServer(applicationContext)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }

        remoteMessage.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "Уведомление",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        when (type) {
            "SCHEDULE_CHANGE" -> handleScheduleChange(data)
            "NEW_NEWS" -> handleNewNews(data)
            "NEW_GRADE" -> handleNewGrade(data)
            else -> showNotification(
                title = data["title"] ?: "Уведомление",
                body = data["body"] ?: "",
                data = data
            )
        }
    }

    private fun handleScheduleChange(data: Map<String, String>) {
        val className = data["class"] ?: ""
        val date = data["date"] ?: ""
        val changes = data["changes"] ?: ""
        showNotification(
            title = "📅 Изменение в расписании",
            body = "В расписании $className на $date: $changes",
            data = data,
            channelId = "schedule_changes"
        )
    }

    private fun handleNewNews(data: Map<String, String>) {
        val title = data["news_title"] ?: "Новая новость"
        val preview = data["preview"] ?: "Опубликована новая новость"
        showNotification(
            title = "📰 $title",
            body = preview,
            data = data,
            channelId = "news_channel"
        )
    }

    private fun handleNewGrade(data: Map<String, String>) {
        val subject = data["subject"] ?: ""
        val grade = data["grade"] ?: ""
        val teacher = data["teacher"] ?: ""
        showNotification(
            title = "📊 Новая оценка",
            body = "По предмету $subject получена оценка $grade (учитель: $teacher)",
            data = data,
            channelId = "grades_channel"
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
        channelId: String = CHANNEL_ID
    ) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android O и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = when (channelId) {
                "schedule_changes" -> "Изменения расписания"
                "news_channel" -> "Новости школы"
                "grades_channel" -> "Оценки"
                else -> CHANNEL_NAME
            }

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал для школьных уведомлений"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("notification_type", data["type"])
            putExtra("notification_data", HashMap(data))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            data["type"]?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_dialog_info) // Замените на свою иконку
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(data.hashCode(), notification)
    }
}