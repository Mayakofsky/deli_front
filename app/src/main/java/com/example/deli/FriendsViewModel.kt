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
    val isSentLoading: Boolean = true,
    val error: String? = null
)

class FriendsViewModel : ViewModel() {
    private val friendRepository = FriendRepository()

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    fun searchUsers(query: String, currentUserId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            try {
                val results = friendRepository.searchUsers(query.trim(), currentUserId)
                _uiState.value = _uiState.value.copy(searchResults = results, isSearching = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false, error = e.message ?: "Ошибка поиска")
            }
        }
    }

    fun sendFriendRequest(userId: String, friendId: String) {
        viewModelScope.launch {
            try {
                friendRepository.sendFriendRequest(userId, friendId)
                _uiState.value = _uiState.value.copy(sentIds = _uiState.value.sentIds + friendId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Ошибка отправки запроса")
            }
        }
    }

    fun respondFriendRequest(userId: String, friendId: String, action: String) {
        viewModelScope.launch {
            try {
                friendRepository.respondFriendRequest(userId, friendId, action)
                loadIncoming(userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Ошибка ответа на запрос")
            }
        }
    }

    fun unsendFriendRequest(userId: String, friendId: String) {
        viewModelScope.launch {
            try {
                friendRepository.unsendFriendRequest(userId, friendId)
                loadSent(userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Ошибка отмены запроса")
            }
        }
    }

    fun loadIncoming(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isIncomingLoading = true, error = null)
            try {
                val items = friendRepository.getIncomingRequests(userId)
                _uiState.value = _uiState.value.copy(incoming = items, isIncomingLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isIncomingLoading = false, error = e.message ?: "Ошибка загрузки входящих")
            }
        }
    }

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFriendsLoading = true, error = null)
            try {
                val items = friendRepository.getFriendsList(userId)
                _uiState.value = _uiState.value.copy(friends = items, isFriendsLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isFriendsLoading = false, error = e.message ?: "Ошибка загрузки друзей")
            }
        }
    }

    fun loadSent(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSentLoading = true, error = null)
            try {
                val items = friendRepository.getOutgoingRequests(userId)
                _uiState.value = _uiState.value.copy(sent = items, isSentLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSentLoading = false, error = e.message ?: "Ошибка загрузки отправленных")
            }
        }
    }
}
