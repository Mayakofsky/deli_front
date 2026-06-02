package com.example.deli

class EventRepository {
    private val api = RetrofitClient.apiService

    suspend fun listEvents(userId: String): List<EventResponse> {
        return api.listEvents(userId)
    }

    suspend fun getEvent(eventId: String): EventResponse {
        return api.getEvent(eventId)
    }

    suspend fun createEvent(creatorId: String, title: String, deadline: String? = null, participantIds: List<String> = emptyList(), guestNames: List<String> = emptyList()): EventResponse {
        return api.createEvent(EventCreateRequest(creatorId, title, deadline, participantIds, guestNames))
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
