package com.example.deli.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

object CacheHelper {
    private var db: SQLiteDatabase? = null

    fun init(context: Context) {
        db = context.openOrCreateDatabase("deli_cache.db", Context.MODE_PRIVATE, null)
        db?.execSQL("CREATE TABLE IF NOT EXISTS events (eventId TEXT PRIMARY KEY, json TEXT NOT NULL, userId TEXT NOT NULL)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS friends (friendId TEXT PRIMARY KEY, json TEXT NOT NULL, ownerId TEXT NOT NULL, listType TEXT NOT NULL)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS debts (debtId TEXT PRIMARY KEY, json TEXT NOT NULL, userId TEXT NOT NULL)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS users (userId TEXT PRIMARY KEY, json TEXT NOT NULL)")
    }

    private fun getDb(): SQLiteDatabase = db ?: throw IllegalStateException("CacheHelper not initialized")

    fun isInitialized(): Boolean = db != null

    // Сохранение в кэш информации о событиях
    fun saveEvents(events: List<String>, userId: String) {
        val d = getDb()
        d.delete("events", "userId = ?", arrayOf(userId))
        events.forEach { json ->
            val id = com.google.gson.JsonParser.parseString(json).asJsonObject.get("id").asString
            d.insertWithOnConflict("events", null, ContentValues().apply {
                put("eventId", id)
                put("json", json)
                put("userId", userId)
            }, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    // Показ всех событий
    fun getEvents(userId: String): List<String> {
        val cursor = getDb().rawQuery("SELECT json FROM events WHERE userId = ?", arrayOf(userId))
        val result = mutableListOf<String>()
        while (cursor.moveToNext()) result.add(cursor.getString(0))
        cursor.close()
        return result
    }

    // Раскрытие события по тапу
    fun getEvent(eventId: String): String? {
        val cursor = getDb().rawQuery("SELECT json FROM events WHERE eventId = ?", arrayOf(eventId))
        val result = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        return result
    }

    // Сохранение в кэш информации о событиях
    fun saveFriends(friends: List<String>, ownerId: String, listType: String) {
        val d = getDb()
        d.delete("friends", "ownerId = ? AND listType = ?", arrayOf(ownerId, listType))
        friends.forEach { json ->
            val id = com.google.gson.JsonParser.parseString(json).asJsonObject.get("user_id").asString
            d.insertWithOnConflict("friends", null, ContentValues().apply {
                put("friendId", id)
                put("json", json)
                put("ownerId", ownerId)
                put("listType", listType)
            }, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    // Показ всех друзей
    fun getFriends(ownerId: String, listType: String): List<String> {
        val cursor = getDb().rawQuery("SELECT json FROM friends WHERE ownerId = ? AND listType = ?", arrayOf(ownerId, listType))
        val result = mutableListOf<String>()
        while (cursor.moveToNext()) result.add(cursor.getString(0))
        cursor.close()
        return result
    }

    // Сохранение в кэш информации о частных долгах
    fun saveDebts(debts: List<String>, userId: String) {
        val d = getDb()
        d.delete("debts", "userId = ?", arrayOf(userId))
        debts.forEach { json ->
            val id = com.google.gson.JsonParser.parseString(json).asJsonObject.get("id").asString
            d.insertWithOnConflict("debts", null, ContentValues().apply {
                put("debtId", id)
                put("json", json)
                put("userId", userId)
            }, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    // Показ всех долгов
    fun getDebts(userId: String): List<String> {
        val cursor = getDb().rawQuery("SELECT json FROM debts WHERE userId = ?", arrayOf(userId))
        val result = mutableListOf<String>()
        while (cursor.moveToNext()) result.add(cursor.getString(0))
        cursor.close()
        return result
    }

    // Сохранение в кэш информации о пользователе (профиль)
    fun saveUser(json: String) {
        val id = com.google.gson.JsonParser.parseString(json).asJsonObject.get("user_id").asString
        getDb().insertWithOnConflict("users", null, ContentValues().apply {
            put("userId", id)
            put("json", json)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Пока информации в профиле
    fun getUser(userId: String): String? {
        val cursor = getDb().rawQuery("SELECT json FROM users WHERE userId = ?", arrayOf(userId))
        val result = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        return result
    }

}