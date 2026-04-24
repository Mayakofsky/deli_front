package com.example.deli.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.deli.MainActivity
import com.example.deli.R

object NotificationHelper {

    // идентификатор канала уведомлений
    private const val CHANNEL_ID = "deli_deadline_channel"

    // название канала уведомлений
    private const val CHANNEL_NAME = "Дедлайны"

    // создает канал уведомлений для android 8 и выше
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // настраивает канал с высоким приоритетом
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                // описание канала
                description = "Уведомления о дедлайнах долгов и событий"
            }

            // регистрирует канал в системе
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    // показывает уведомление с заголовком и текстом
    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        // интент открывает главный экран по нажатию на уведомление
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // оборачивает интент для безопасного запуска из уведомления
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // собирает уведомление с иконкой, заголовком и текстом
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // иконка уведомления
            .setSmallIcon(R.mipmap.ic_launcher)

            // заголовок уведомления
            .setContentTitle(title)

            // краткий текст уведомления
            .setContentText(message)

            // разворачиваемый полный текст уведомления
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

            // высокий приоритет для немедленного показа
            .setPriority(NotificationCompat.PRIORITY_HIGH)

            // действие по нажатию на уведомление
            .setContentIntent(pendingIntent)

            // автоматически скрывает уведомление после нажатия
            .setAutoCancel(true)
            .build()

        // отправляет уведомление в систему
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(notificationId, notification)
    }
}