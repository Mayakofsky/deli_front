package com.example.deli

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val api = RetrofitClient.apiService
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    override suspend fun doWork(): Result {
        NotificationHelper.createNotificationChannels(applicationContext)
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
        Log.d(TAG, "Проверка уведомлений для userId=$userId")

        try {
            checkFriendRequests(userId)
            checkDebtDeadlines(userId)
            checkEventDeadlines(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка: ${e.message}")
            return Result.retry()
        }

        return Result.success()
    }

    private suspend fun checkFriendRequests(userId: String) {
        val incoming = api.getIncomingRequests(userId)
        val currentCount = incoming.size
        val lastCount = NotificationPrefs.getLastFriendCount(applicationContext)

        if (lastCount == -1) {
            NotificationPrefs.setLastFriendCount(applicationContext, currentCount)
            return
        }

        if (currentCount > lastCount) {
            val newRequests = incoming.drop(lastCount)
            for (request in newRequests) {
                val fullName = "${request.first_name} ${request.last_name}".trim()
                NotificationHelper.showFriendRequestNotification(
                    applicationContext, fullName, currentCount - lastCount
                )
            }
        }

        NotificationPrefs.setLastFriendCount(applicationContext, currentCount)
    }

    private suspend fun checkDebtDeadlines(userId: String) {
        val debts = api.listDebts(userId)
        val notifiedIds = NotificationPrefs.getNotifiedDebtIds(applicationContext)
        val newNotifiedIds = mutableSetOf<String>()

        val today = System.currentTimeMillis()
        val dayMillis = 86_400_000L

        for (debt in debts) {
            val deadlineStr = debt.deadline ?: continue
            val deadlineMillis = parseDate(deadlineStr) ?: continue
            val diffDays = ((deadlineMillis - today) / dayMillis).toInt()

            if (diffDays < 0) continue

            if (diffDays <= 3 && diffDays > 0 && debt.id !in notifiedIds) {
                val isOwed = debt.creditor?.user_id == userId
                val counterparty = if (isOwed) debt.debtor else debt.creditor
                val name = counterparty?.let { "${it.first_name} ${it.last_name}".trim() } ?: "Пользователь"

                NotificationHelper.showDebtDeadlineNotification(
                    applicationContext, name, debt.amount, diffDays, isOwed, debt.id
                )
                newNotifiedIds.add(debt.id)
            }
        }

        if (newNotifiedIds.isNotEmpty()) {
            NotificationPrefs.addNotifiedDebtIds(applicationContext, newNotifiedIds)
        }
    }

    private suspend fun checkEventDeadlines(userId: String) {
        val events = api.listEvents(userId)
        val notifiedIds = NotificationPrefs.getNotifiedEventIds(applicationContext)
        val newNotifiedIds = mutableSetOf<String>()

        val today = System.currentTimeMillis()
        val dayMillis = 86_400_000L

        for (event in events) {
            val deadlineStr = event.deadline ?: continue
            if (event.status == "closed") continue

            val deadlineMillis = parseDate(deadlineStr) ?: continue
            val diffDays = ((deadlineMillis - today) / dayMillis).toInt()

            if (diffDays < 0) continue

            if (diffDays <= 3 && diffDays > 0 && event.id !in notifiedIds) {
                NotificationHelper.showEventDeadlineNotification(
                    applicationContext, event.title, diffDays, event.id
                )
                newNotifiedIds.add(event.id)
            }
        }

        if (newNotifiedIds.isNotEmpty()) {
            NotificationPrefs.addNotifiedEventIds(applicationContext, newNotifiedIds)
        }
    }

    private fun parseDate(dateStr: String): Long? {
        return try {
            dateFormat.parse(dateStr)?.time
        } catch (e: Exception) {
            try {
                val simpleFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                simpleFormat.parse(dateStr)?.time
            } catch (e2: Exception) {
                null
            }
        }
    }

    companion object {
        const val TAG = "NotificationWorker"
        const val KEY_USER_ID = "user_id"
        private const val WORK_NAME = "notification_check"

        fun schedule(context: Context, userId: String) {
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
                15, TimeUnit.MINUTES
            ).setInputData(
                androidx.work.Data.Builder()
                    .putString(KEY_USER_ID, userId)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
            )
            Log.d(TAG, "Запланирован PeriodicWork для userId=$userId")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "PeriodicWork отменён")
        }
    }
}
