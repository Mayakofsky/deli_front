package com.example.deli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DebtUiState(
    val counterpartyLink: String? = null,
    val linkLoading: Boolean = true
)

class DebtViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val debtRepository = DebtRepository()

    private val _uiState = MutableStateFlow(DebtUiState())
    val uiState: StateFlow<DebtUiState> = _uiState.asStateFlow()

    fun loadCounterpartyLink(userId: String?) {
        if (userId == null) {
            _uiState.value = DebtUiState(linkLoading = false)
            return
        }
        viewModelScope.launch {
            _uiState.value = DebtUiState(linkLoading = true)
            try {
                val user = userRepository.getUser(userId)
                _uiState.value = DebtUiState(counterpartyLink = user.link, linkLoading = false)
            } catch (_: Exception) {
                _uiState.value = DebtUiState(linkLoading = false)
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
