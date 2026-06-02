package com.example.deli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventUiState(
    val event: EventResponse? = null,
    val purchases: List<PurchaseResponse> = emptyList(),
    val balances: List<BalanceItem> = emptyList(),
    val participantLinks: Map<String, String?> = emptyMap(),
    val isLoading: Boolean = true,
    val linksLoading: Boolean = false
)

class EventViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private var currentEventId: String = ""

    fun loadEvent(eventId: String) {
        currentEventId = eventId
        viewModelScope.launch {
            _uiState.value = EventUiState(isLoading = true)
            try {
                val event = eventRepository.getEvent(eventId)
                val purchases = eventRepository.listPurchases(eventId)
                val balances = eventRepository.getBalances(eventId)
                _uiState.value = EventUiState(event = event, purchases = purchases, balances = balances)
            } catch (_: Exception) {
                _uiState.value = EventUiState(isLoading = false)
            }
        }
    }

    fun loadParticipantLinks(userId: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val creditorIds = state.balances.filter { it.balance > 0 }.map { it.user_id }
            if (creditorIds.isEmpty()) return@launch
            _uiState.value = state.copy(linksLoading = true)
            try {
                val links = coroutineScope {
                    creditorIds.map { id ->
                        async {
                            try {
                                val user = userRepository.getUser(id)
                                id to user.link
                            } catch (_: Exception) {
                                id to null
                            }
                        }
                    }.awaitAll().toMap()
                }
                _uiState.value = _uiState.value.copy(participantLinks = links, linksLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(linksLoading = false)
            }
        }
    }

    fun closeEvent() {
        viewModelScope.launch {
            try {
                eventRepository.closeEvent(currentEventId)
                loadEvent(currentEventId)
            } catch (_: Exception) {}
        }
    }

    fun removeParticipant(eventId: String, userId: String) {
        viewModelScope.launch {
            try {
                eventRepository.removeParticipant(eventId, userId)
                loadEvent(eventId)
            } catch (_: Exception) {}
        }
    }

    fun addParticipant(eventId: String, userId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                eventRepository.addParticipant(eventId, userId)
                onDone()
                loadEvent(eventId)
            } catch (_: Exception) {}
        }
    }

    suspend fun getFriendsList(userId: String): List<FriendUser> {
        return eventRepository.getFriendsList(userId)
    }

    fun addGuest(eventId: String, name: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                eventRepository.addGuest(eventId, name)
                onDone()
                loadEvent(eventId)
            } catch (_: Exception) {}
        }
    }

    fun updatePurchase(eventId: String, purchaseId: String, request: PurchaseUpdateRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                eventRepository.updatePurchase(eventId, purchaseId, request)
                onDone()
                loadEvent(eventId)
            } catch (_: Exception) {}
        }
    }
}
