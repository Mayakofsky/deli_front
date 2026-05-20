package com.example.deli.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Дедлайн"
        val message = intent.getStringExtra("message") ?: "Скоро дедлайн"
        val notificationId = intent.getIntExtra("notificationId", 0)

        NotificationHelper.createChannel(context)
        NotificationHelper.showNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            message = message
        )
    }
}