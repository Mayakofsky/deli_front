package com.example.deli

import com.example.deli.data.CacheHelper
import com.google.gson.Gson

class FriendRepository {
    private val api = RetrofitClient.apiService
    private val gson = Gson()

    suspend fun searchUsers(query: String, currentUserId: String): List<FriendUser> {
        return api.searchUsers(query, currentUserId)
    }

    suspend fun sendFriendRequest(userId: String, friendId: String) {
        api.sendFriendRequest(FriendSendRequest(userId, friendId))
    }

    suspend fun unsendFriendRequest(userId: String, friendId: String) {
        api.unsendFriendRequest(FriendSendRequest(userId, friendId))
    }

    suspend fun respondFriendRequest(userId: String, friendId: String, action: String) {
        api.respondFriendRequest(FriendRespondRequest(userId, friendId, action))
    }

    suspend fun getIncomingRequests(userId: String): List<FriendUser> {
        return try {
            val data = api.getIncomingRequests(userId)
            try {
                CacheHelper.saveFriends(data.map { gson.toJson(it) }, userId, "incoming")
            } catch (_: Exception) {}
            data
        } catch (e: Exception) {
            if (CacheHelper.isInitialized()) {
                val cached = CacheHelper.getFriends(userId, "incoming")
                if (cached.isNotEmpty()) return cached.map { gson.fromJson(it, FriendUser::class.java) }
            }
            throw e
        }
    }

    suspend fun getOutgoingRequests(userId: String): List<FriendUser> {
        return try {
            val data = api.getOutgoingRequests(userId)
            try {
                CacheHelper.saveFriends(data.map { gson.toJson(it) }, userId, "outgoing")
            } catch (_: Exception) {}
            data
        } catch (e: Exception) {
            if (CacheHelper.isInitialized()) {
                val cached = CacheHelper.getFriends(userId, "outgoing")
                if (cached.isNotEmpty()) return cached.map { gson.fromJson(it, FriendUser::class.java) }
            }
            throw e
        }
    }

    suspend fun getFriendsList(userId: String): List<FriendUser> {
        return try {
            val data = api.getFriendsList(userId)
            try {
                CacheHelper.saveFriends(data.map { gson.toJson(it) }, userId, "friends")
            } catch (_: Exception) {}
            data
        } catch (e: Exception) {
            if (CacheHelper.isInitialized()) {
                val cached = CacheHelper.getFriends(userId, "friends")
                if (cached.isNotEmpty()) return cached.map { gson.fromJson(it, FriendUser::class.java) }
            }
            throw e
        }
    }
}
