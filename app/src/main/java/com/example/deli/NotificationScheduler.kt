package com.example.deli.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificationScheduler {

    // планирует уведомления за неделю и за день до дедлайна
    fun scheduleDeadlineNotifications(
        context: Context,
        title: String,
        message: String,
        deadline: String,
        baseId: Int
    ) {
        // пропускает если дата не указана
        if (deadline.isBlank()) return

        try {
            // парсит строку даты в объект Date
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val deadlineDate = sdf.parse(deadline) ?: return

            // устанавливает время дедлайна на 10 утра
            val calendar = Calendar.getInstance().apply {
                time = deadlineDate
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // время дедлайна в миллисекундах
            val deadlineMillis = calendar.timeInMillis

            // текущее время в миллисекундах
            val now = System.currentTimeMillis()

            // считает время за неделю до дедлайна
            val weekBefore = deadlineMillis - 7L * 24 * 60 * 60 * 1000

            // планирует уведомление за неделю если время еще не прошло
            if (weekBefore > now) {
                scheduleAlarm(
                    context = context,
                    triggerAt = weekBefore,
                    title = "📅 Через неделю дедлайн",
                    message = message,
                    notificationId = baseId + 1
                )
            }

            // считает время за день до дедлайна
            val dayBefore = deadlineMillis - 24L * 60 * 60 * 1000

            // планирует уведомление за день если время еще не прошло
            if (dayBefore > now) {
                scheduleAlarm(
                    context = context,
                    triggerAt = dayBefore,
                    title = "⏰ Завтра дедлайн!",
                    message = message,
                    notificationId = baseId + 2
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // создает системный будильник для отправки уведомления в нужное время
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