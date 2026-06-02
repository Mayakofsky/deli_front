package com.example.deli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateEventUiState(
    val friends: List<FriendUser> = emptyList(),
    val friendsLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val createdEventId: String? = null
)

class CreateEventViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val friendRepository = FriendRepository()

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(friendsLoading = true)
            try {
                val items = friendRepository.getFriendsList(userId)
                _uiState.value = _uiState.value.copy(friends = items, friendsLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(friendsLoading = false)
            }
        }
    }

    fun createEvent(creatorId: String, title: String, deadline: String?, participantIds: List<String>, guestNames: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            try {
                val response = eventRepository.createEvent(creatorId, title, deadline, participantIds, guestNames)
                _uiState.value = _uiState.value.copy(createdEventId = response.id, isCreating = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка: ${e.message}", isCreating = false)
            }
        }
    }
}
