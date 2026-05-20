package com.example.deli.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// получает системный broadcast и показывает уведомление
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // получает заголовок из интента или подставляет значение по умолчанию
        val title = intent.getStringExtra("title") ?: "Дедлайн"

        // получает текст сообщения из интента или подставляет значение по умолчанию
        val message = intent.getStringExtra("message") ?: "Скоро дедлайн"

        // получает идентификатор уведомления из интента
        val notificationId = intent.getIntExtra("notificationId", 0)

        // создает канал уведомлений если он еще не создан
        NotificationHelper.createChannel(context)

        // показывает уведомление с полученными данными
        NotificationHelper.showNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            message = message
        )
    }
}