package com.example.deli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUiState(
    val searchResults: List<FriendUser> = emptyList(),
    val incoming: List<FriendUser> = emptyList(),
    val friends: List<FriendUser> = emptyList(),
    val sent: List<FriendUser> = emptyList(),
    val sentIds: Set<String> = emptySet(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = true,
    val isIncomingLoading: Boolean = true,
    val isFriendsLoading: Boolean = true,
    val isSentLoading: Boolean = true
)

class FriendsViewModel : ViewModel() {
    private val friendRepository = FriendRepository()

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    fun searchUsers(query: String, currentUserId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                val results = friendRepository.searchUsers(query.trim(), currentUserId)
                _uiState.value = _uiState.value.copy(searchResults = results, isSearching = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            }
        }
    }

    fun sendFriendRequest(userId: String, friendId: String) {
        viewModelScope.launch {
            try {
                friendRepository.sendFriendRequest(userId, friendId)
                _uiState.value = _uiState.value.copy(sentIds = _uiState.value.sentIds + friendId)
            } catch (_: Exception) {}
        }
    }

    fun respondFriendRequest(userId: String, friendId: String, action: String) {
        viewModelScope.launch {
            try {
                friendRepository.respondFriendRequest(userId, friendId, action)
                loadIncoming(userId)
            } catch (_: Exception) {}
        }
    }

    fun unsendFriendRequest(userId: String, friendId: String) {
        viewModelScope.launch {
            try {
                friendRepository.unsendFriendRequest(userId, friendId)
                loadSent(userId)
            } catch (_: Exception) {}
        }
    }

    fun loadIncoming(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isIncomingLoading = true)
            try {
                val items = friendRepository.getIncomingRequests(userId)
                _uiState.value = _uiState.value.copy(incoming = items, isIncomingLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isIncomingLoading = false)
            }
        }
    }

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFriendsLoading = true)
            try {
                val items = friendRepository.getFriendsList(userId)
                _uiState.value = _uiState.value.copy(friends = items, isFriendsLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isFriendsLoading = false)
            }
        }
    }

    fun loadSent(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSentLoading = true)
            try {
                val items = friendRepository.getOutgoingRequests(userId)
                _uiState.value = _uiState.value.copy(sent = items, isSentLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSentLoading = false)
            }
        }
    }
}
