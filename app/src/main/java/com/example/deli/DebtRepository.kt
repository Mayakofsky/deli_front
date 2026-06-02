package com.example.deli

class DebtRepository {
    private val api = RetrofitClient.apiService

    suspend fun createDebt(request: DebtCreateRequest): DebtResponse {
        return api.createDebt(request)
    }

    suspend fun listDebts(userId: String, status: String? = null): List<DebtResponse> {
        return api.listDebts(userId, status)
    }

    suspend fun updateDebt(debtId: String, request: DebtUpdateRequest) {
        api.updateDebt(debtId, request)
    }

    suspend fun deleteDebt(debtId: String) {
        api.deleteDebt(debtId)
    }

    suspend fun summaryOwed(userId: String): List<SummaryItem> {
        return api.summaryOwed(userId)
    }

    suspend fun summaryDue(userId: String): List<SummaryItem> {
        return api.summaryDue(userId)
    }

    suspend fun createGuest(name: String): EventParticipant {
        return api.createGuest(GuestCreateRequest(name))
    }
}
