package com.example.deli.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore by preferencesDataStore(name = "notification_prefs")

private val LAST_FRIEND_COUNT_KEY = intPreferencesKey("last_friend_count")
private val NOTIFIED_DEBT_IDS_KEY = stringSetPreferencesKey("notified_debt_ids")
private val NOTIFIED_EVENT_IDS_KEY = stringSetPreferencesKey("notified_event_ids")

object NotificationPrefs {

    suspend fun getLastFriendCount(context: Context): Int {
        return context.notificationDataStore.data.map { prefs ->
            prefs[LAST_FRIEND_COUNT_KEY] ?: -1
        }.first()
    }

    suspend fun setLastFriendCount(context: Context, count: Int) {
        context.notificationDataStore.edit { prefs ->
            prefs[LAST_FRIEND_COUNT_KEY] = count
        }
    }

    suspend fun getNotifiedDebtIds(context: Context): Set<String> {
        return context.notificationDataStore.data.map { prefs ->
            prefs[NOTIFIED_DEBT_IDS_KEY] ?: emptySet()
        }.first()
    }

    suspend fun addNotifiedDebtIds(context: Context, ids: Set<String>) {
        context.notificationDataStore.edit { prefs ->
            val existing = prefs[NOTIFIED_DEBT_IDS_KEY] ?: emptySet()
            prefs[NOTIFIED_DEBT_IDS_KEY] = existing + ids
        }
    }

    suspend fun getNotifiedEventIds(context: Context): Set<String> {
        return context.notificationDataStore.data.map { prefs ->
            prefs[NOTIFIED_EVENT_IDS_KEY] ?: emptySet()
        }.first()
    }

    suspend fun addNotifiedEventIds(context: Context, ids: Set<String>) {
        context.notificationDataStore.edit { prefs ->
            val existing = prefs[NOTIFIED_EVENT_IDS_KEY] ?: emptySet()
            prefs[NOTIFIED_EVENT_IDS_KEY] = existing + ids
        }
    }

    suspend fun clearAll(context: Context) {
        context.notificationDataStore.edit { it.clear() }
    }
}
