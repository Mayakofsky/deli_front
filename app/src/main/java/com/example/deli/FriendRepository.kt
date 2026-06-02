package com.example.deli

class FriendRepository {
    private val api = RetrofitClient.apiService

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
        return api.getIncomingRequests(userId)
    }

    suspend fun getOutgoingRequests(userId: String): List<FriendUser> {
        return api.getOutgoingRequests(userId)
    }

    suspend fun getFriendsList(userId: String): List<FriendUser> {
        return api.getFriendsList(userId)
    }
}
