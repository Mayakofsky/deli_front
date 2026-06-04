package com.example.deli

import com.example.deli.data.CacheHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EventRepository {
    private val api = RetrofitClient.apiService
    private val gson = Gson()

    suspend fun listEvents(userId: String): List<EventResponse> {
        return try {
            val data = api.listEvents(userId)
            try {
                val json = data.map { gson.toJson(it) }
                CacheHelper.saveEvents(json, userId)
            } catch (_: Exception) {}
            data
        } catch (e: Exception) {
            if (CacheHelper.isInitialized()) {
                val cached = CacheHelper.getEvents(userId)
                if (cached.isNotEmpty()) {
                    return cached.map { gson.fromJson(it, EventResponse::class.java) }
                }
            }
            throw e
        }
    }

    suspend fun getEvent(eventId: String): EventResponse {
        return try {
            val data = api.getEvent(eventId)
            try {
                val json = gson.toJson(data)
                CacheHelper.saveEvents(listOf(json), data.creator_id)
            } catch (_: Exception) {}
            data
        } catch (e: Exception) {
            if (CacheHelper.isInitialized()) {
                val cached = CacheHelper.getEvent(eventId)
                if (cached != null) return gson.fromJson(cached, EventResponse::class.java)
            }
            throw e
        }
    }

    suspend fun createEvent(creatorId: String, title: String, deadline: String? = null, participantIds: List<String> = emptyList(), guestNames: List<String> = emptyList()): EventResponse {
        return api.createEvent(EventCreateRequest(creatorId, title, deadline, participantIds, guestNames))
    }

    suspend fun getConfirmations(eventId: String): List<String> {
        return api.getEventConfirmations(eventId)
    }

    suspend fun confirmEvent(eventId: String, userId: String): EventResponse {
        return api.confirmEvent(eventId, EventConfirmRequest(userId))
    }

    suspend fun deleteEvent(eventId: String) {
        api.deleteEvent(eventId)
    }

    suspend fun closeEvent(eventId: String) {
        api.closeEvent(eventId)
    }

    suspend fun listParticipants(eventId: String): List<EventParticipant> {
        return api.listParticipants(eventId)
    }

    suspend fun addParticipant(eventId: String, userId: String) {
        api.addParticipant(eventId, ParticipantAddRequest(userId))
    }

    suspend fun removeParticipant(eventId: String, userId: String) {
        api.removeParticipant(eventId, userId)
    }

    suspend fun addGuest(eventId: String, name: String) {
        api.addGuest(eventId, GuestCreateRequest(name))
    }

    suspend fun listPurchases(eventId: String): List<PurchaseResponse> {
        return api.listPurchases(eventId)
    }

    suspend fun addPurchase(eventId: String, request: PurchaseCreateRequest): PurchaseResponse {
        return api.addPurchase(eventId, request)
    }

    suspend fun updatePurchase(eventId: String, purchaseId: String, request: PurchaseUpdateRequest): PurchaseResponse {
        return api.updatePurchase(eventId, purchaseId, request)
    }

    suspend fun getBalances(eventId: String): List<BalanceItem> {
        return api.getBalances(eventId)
    }

    suspend fun getFriendsList(userId: String): List<FriendUser> {
        return api.getFriendsList(userId)
    }
}
