package com.example.deli.viewmodel

import com.example.deli.model.FriendUser
import com.example.deli.repository.EventRepository
import com.example.deli.repository.FriendRepository
import com.example.deli.repository.UserRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateEventUiState(
    val friends: List<FriendUser> = emptyList(),
    val friendsLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val createdEventId: String? = null,
    val currentUserLink: String? = null,
    val linkLoaded: Boolean = false
)

class CreateEventViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val friendRepository = FriendRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    private suspend fun enrichWithPhotoUrl(items: List<FriendUser>): List<FriendUser> {
        return coroutineScope {
            items.map { friend ->
                async {
                    try {
                        val user = userRepository.getUser(friend.user_id)
                        friend.copy(photo_url = user.photo_url)
                    } catch (_: Exception) { friend }
                }
            }.awaitAll()
        }
    }

    fun loadFriends(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(friendsLoading = true)
            try {
                val items = friendRepository.getFriendsList(userId)
                val enriched = enrichWithPhotoUrl(items)
                _uiState.value = _uiState.value.copy(friends = enriched, friendsLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(friendsLoading = false)
            }
        }
    }

    fun loadCurrentUser(userId: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(userId)
                _uiState.value = _uiState.value.copy(currentUserLink = user.link, linkLoaded = true)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(linkLoaded = true)
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
