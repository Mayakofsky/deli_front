package com.example.deli.viewmodel

import com.example.deli.model.DebtResponse
import com.example.deli.model.DebtUpdateRequest
import com.example.deli.repository.DebtRepository
import com.example.deli.repository.UserRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DebtUiState(
    val counterpartyLink: String? = null,
    val counterpartyPhotoUrl: String? = null,
    val linkLoading: Boolean = true,
    val debtDetail: DebtResponse? = null,
    val isLoadingDetail: Boolean = false,
    val debtorFirstPress: Boolean = false,
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
                _uiState.value = _uiState.value.copy(
                    counterpartyLink = user.link,
                    counterpartyPhotoUrl = user.photo_url,
                    linkLoading = false
                )
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

    fun onDebtorFirstPress() {
        _uiState.value = _uiState.value.copy(debtorFirstPress = true)
    }

    fun confirmTransfer(debtId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            try {
                debtRepository.updateDebt(debtId, DebtUpdateRequest(payment_photo_url = "confirmed"))
                loadDebtDetail(debtId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка: ${e.message}")
            }
        }
    }

    fun closeDebt(debtId: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            try {
                if (debtId != null) debtRepository.deleteDebt(debtId)
                onDone()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка: ${e.message}")
            }
        }
    }
}
