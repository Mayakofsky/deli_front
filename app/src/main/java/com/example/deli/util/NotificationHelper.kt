package com.example.deli.util

import com.example.deli.MainActivity

import android.Manifest

//noinspection SuspiciousImport
import android.R
import kotlin.math.abs
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {

    const val CHANNEL_FRIEND_REQUESTS = "friend_requests"
    const val CHANNEL_DEADLINES = "deadlines"

    private const val FRIEND_REQUEST_NOTIFICATION_ID = 1001
    private const val DEBT_DEADLINE_BASE_ID = 2000
    private const val EVENT_DEADLINE_BASE_ID = 3000

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels(context: Context) {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_FRIEND_REQUESTS,
                "Запросы в друзья",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Уведомления о новых запросах в друзья" },
            NotificationChannel(
                CHANNEL_DEADLINES,
                "Дедлайны",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Уведомления о приближающихся дедлайнах долгов и событий" }
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { manager.createNotificationChannel(it) }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showFriendRequestNotification(context: Context, friendName: String, count: Int) {
        if (!hasPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FRIEND_REQUESTS)
            .setSmallIcon(R.drawable.ic_input_add)
            .setContentTitle("Новый запрос в друзья")
            .setContentText("У вас новый запрос в друзья от $friendName")
            .setStyle(
                if (count > 1) NotificationCompat.InboxStyle()
                    .addLine("У вас $count новых запроса в друзья")
                else null
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(FRIEND_REQUEST_NOTIFICATION_ID, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDebtDeadlineNotification(
        context: Context,
        counterpartyName: String,
        amount: Double,
        daysLeft: Int,
        isOwed: Boolean,
        debtId: String
    ) {
        if (!hasPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Скоро дедлайн долга"
        val body = if (isOwed) {
            "$counterpartyName должен вам ${amount}₽ — осталось $daysLeft дн."
        } else {
            "Вы должны $counterpartyName ${amount}₽ — осталось $daysLeft дн."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_DEADLINES)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = DEBT_DEADLINE_BASE_ID + abs(debtId.hashCode() % 1000)
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showEventDeadlineNotification(
        context: Context,
        eventTitle: String,
        daysLeft: Int,
        eventId: String
    ) {
        if (!hasPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DEADLINES)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Скоро дедлайн события")
            .setContentText("Событие «$eventTitle» заканчивается через $daysLeft дн.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = EVENT_DEADLINE_BASE_ID + abs(eventId.hashCode() % 1000)
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }
}
