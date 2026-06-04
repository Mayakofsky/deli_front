package com.example.deli

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DebtUiState(
    val counterpartyLink: String? = null,
    val linkLoading: Boolean = true,
    val debtDetail: DebtResponse? = null,
    val isLoadingDetail: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null
)

class DebtViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val debtRepository = DebtRepository()

    private val _uiState = MutableStateFlow(DebtUiState())
    val uiState: StateFlow<DebtUiState> = _uiState.asStateFlow()

    fun loadCounterpartyLink(userId: String?) {
        if (userId == null) {
            _uiState.value = _uiState.value.copy(linkLoading = false)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(linkLoading = true)
            try {
                val user = userRepository.getUser(userId)
                _uiState.value = _uiState.value.copy(counterpartyLink = user.link, linkLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(linkLoading = false)
            }
        }
    }

    fun loadDebtDetail(debtId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDetail = true, error = null)
            try {
                val debt = debtRepository.getDebt(debtId)
                _uiState.value = _uiState.value.copy(debtDetail = debt, isLoadingDetail = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingDetail = false)
            }
        }
    }

    fun submitPaymentProof(debtId: String, context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)
            try {
                val photoUrl = userRepository.uploadPhoto(context, uri)
                debtRepository.updateDebt(debtId, DebtUpdateRequest(
                    status = "awaiting_confirmation",
                    payment_photo_url = photoUrl
                ))
                _uiState.value = _uiState.value.copy(isUploading = false)
                loadDebtDetail(debtId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUploading = false, error = "Ошибка: ${e.message}")
            }
        }
    }

    fun confirmPayment(debtId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            try {
                debtRepository.updateDebt(debtId, DebtUpdateRequest(status = "paid"))
                loadDebtDetail(debtId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка: ${e.message}")
            }
        }
    }

    fun deleteDebt(debtId: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                if (debtId != null) debtRepository.deleteDebt(debtId)
                onDone()
            } catch (_: Exception) {}
        }
    }
}
