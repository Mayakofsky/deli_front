package com.example.deli.repository

import com.example.deli.data.CacheHelper
import com.example.deli.model.DebtCreateRequest
import com.example.deli.model.DebtResponse
import com.example.deli.model.DebtUpdateRequest
import com.example.deli.model.EventParticipant
import com.example.deli.model.GuestCreateRequest
import com.example.deli.model.SummaryItem
import com.example.deli.network.RetrofitClient
import com.google.gson.Gson

class DebtRepository {
    private val api = RetrofitClient.apiService
    private val gson = Gson()

    suspend fun createDebt(request: DebtCreateRequest): DebtResponse {
        return api.createDebt(request)
    }

    suspend fun listDebts(userId: String, status: String? = null): List<DebtResponse> {
        return try {
            val data = api.listDebts(userId, status)
            try {
                CacheHelper.saveDebts(data.map { gson.toJson(it) }, userId)
            } catch (_: Exception) {}
            data
        } catch (e: Exception) {
            if (CacheHelper.isInitialized()) {
                val cached = CacheHelper.getDebts(userId)
                if (cached.isNotEmpty()) return cached.map { gson.fromJson(it, DebtResponse::class.java) }
            }
            throw e
        }
    }

    suspend fun getDebt(debtId: String): DebtResponse {
        return api.getDebt(debtId)
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
