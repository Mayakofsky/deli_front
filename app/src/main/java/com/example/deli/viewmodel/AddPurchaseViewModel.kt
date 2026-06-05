package com.example.deli.viewmodel

import com.example.deli.model.EventParticipant
import com.example.deli.model.PurchaseCreateRequest
import com.example.deli.repository.EventRepository
import com.example.deli.repository.UserRepository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddPurchaseUiState(
    val participants: List<EventParticipant> = emptyList(),
    val isUploading: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val photoUrl: String? = null
)

class AddPurchaseViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(AddPurchaseUiState())
    val uiState: StateFlow<AddPurchaseUiState> = _uiState.asStateFlow()

    fun loadParticipants(eventId: String, excludeUserId: String) {
        viewModelScope.launch {
            try {
                val participants = eventRepository.listParticipants(eventId)
                _uiState.value = _uiState.value.copy(participants = participants)
            } catch (_: Exception) {}
        }
    }

    fun uploadPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)
            try {
                val url = userRepository.uploadPhoto(context, uri)
                _uiState.value = _uiState.value.copy(photoUrl = url, isUploading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка загрузки фото: ${e.message}", isUploading = false)
            }
        }
    }

    fun addPurchase(eventId: String, userId: String, description: String, amount: Double, photoUrl: String?, beneficiaryIds: List<String>, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                eventRepository.addPurchase(eventId, PurchaseCreateRequest(
                    buyer_id = userId,
                    description = description,
                    amount = amount,
                    receipt_photo_url = photoUrl,
                    beneficiary_ids = beneficiaryIds
                ))
                onDone()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка: ${e.message}", isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
