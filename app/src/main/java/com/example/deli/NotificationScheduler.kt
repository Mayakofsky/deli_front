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

    /**
     * Запланировать уведомления за неделю и за день до дедлайна
     * @param deadline дата в формате "dd.MM.yyyy"
     */
    fun scheduleDeadlineNotifications(
        context: Context,
        title: String,
        message: String,
        deadline: String,
        baseId: Int
    ) {
        if (deadline.isBlank()) return

        try {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val deadlineDate = sdf.parse(deadline) ?: return

            val calendar = Calendar.getInstance().apply {
                time = deadlineDate
                set(Calendar.HOUR_OF_DAY, 10)  // в 10 утра
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            val deadlineMillis = calendar.timeInMillis
            val now = System.currentTimeMillis()

            // За неделю
            val weekBefore = deadlineMillis - 7L * 24 * 60 * 60 * 1000
            if (weekBefore > now) {
                scheduleAlarm(
                    context = context,
                    triggerAt = weekBefore,
                    title = "📅 Через неделю дедлайн",
                    message = message,
                    notificationId = baseId + 1
                )
            }

            // За день
            val dayBefore = deadlineMillis - 24L * 60 * 60 * 1000
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

    private fun scheduleAlarm(
        context: Context,
        triggerAt: Long,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationId", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    /**
     * Отменить уведомления
     */
    fun cancelDeadlineNotifications(context: Context, baseId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        listOf(baseId + 1, baseId + 2).forEach { id ->
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}