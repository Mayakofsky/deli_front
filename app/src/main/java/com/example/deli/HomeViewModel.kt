package com.example.deli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val owedItems: List<SummaryItem> = emptyList(),
    val dueItems: List<SummaryItem> = emptyList(),
    val events: List<EventResponse> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val debtRepository = DebtRepository()
    private val eventRepository = EventRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadData(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val owed = debtRepository.summaryOwed(userId)
                val due = debtRepository.summaryDue(userId)
                val evts = eventRepository.listEvents(userId)
                _uiState.value = HomeUiState(owedItems = owed, dueItems = due, events = evts, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun refresh(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            try {
                val owed = debtRepository.summaryOwed(userId)
                val due = debtRepository.summaryDue(userId)
                val evts = eventRepository.listEvents(userId)
                _uiState.value = HomeUiState(owedItems = owed, dueItems = due, events = evts, isRefreshing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRefreshing = false, error = e.message ?: "Ошибка загрузки")
            }
        }
    }
}
