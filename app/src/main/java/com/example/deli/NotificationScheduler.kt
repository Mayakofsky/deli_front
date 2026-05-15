package com.example.deli.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
object NotificationScheduler {
    private fun scheduleAlarm(
        context: Context,
        triggerAt: Long,
        title: String,
        message: String,
        notificationId: Int
    ) {
        // интент передает данные уведомления в broadcast receiver
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationId", notificationId)
        }

        // оборачивает интент для запуска через AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // получает системный менеджер будильников
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                // планирует точный будильник с пробуждением экрана
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                // планирует неточный будильник если нет разрешения
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
        } else {
            // для старых версий android планирует точный будильник напрямую
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    // отменяет все запланированные уведомления для конкретного долга или события
    fun cancelDeadlineNotifications(context: Context, baseId: Int) {

        // получает системный менеджер будильников
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // отменяет будильники за неделю и за день
        listOf(baseId + 1, baseId + 2).forEach { id ->
            val intent = Intent(context, NotificationReceiver::class.java)

            // ищет существующий pendingIntent по идентификатору
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )

            // отменяет будильник если он существует
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}