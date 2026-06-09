package com.example.deli.viewmodel

import com.example.deli.model.BalanceItem
import com.example.deli.model.EventParticipant
import com.example.deli.model.EventResponse
import com.example.deli.model.FriendUser
import com.example.deli.model.PurchaseResponse
import com.example.deli.model.PurchaseUpdateRequest
import com.example.deli.repository.EventRepository
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

data class EventUiState(
    val event: EventResponse? = null,
    val purchases: List<PurchaseResponse> = emptyList(),
    val balances: List<BalanceItem> = emptyList(),
    val participantLinks: Map<String, String?> = emptyMap(),
    val isLoading: Boolean = true,
    val linksLoading: Boolean = false,
    val confirmFirstPress: Map<String, Boolean> = emptyMap(),
    val error: String? = null
)

class EventViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private var currentEventId: String = ""

    private suspend fun enrichParticipants(participants: List<EventParticipant>?): List<EventParticipant>? {
        if (participants == null) return null
        return coroutineScope {
            participants.map { p ->
                async {
                    try {
                        val user = userRepository.getUser(p.user_id)
                        p.copy(photo_url = user.photo_url)
                    } catch (_: Exception) { p }
                }
            }.awaitAll()
        }
    }

    fun loadEvent(eventId: String) {
        currentEventId = eventId
        viewModelScope.launch {
            _uiState.value = EventUiState(isLoading = true, error = null)
            try {
                val event = eventRepository.getEvent(eventId)
                val purchases = eventRepository.listPurchases(eventId)
                val balances = eventRepository.getBalances(eventId)
                val enrichedParticipants = enrichParticipants(event.participants)
                val enrichedEvent = event.copy(participants = enrichedParticipants)
                _uiState.value = EventUiState(event = enrichedEvent, purchases = purchases, balances = balances, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = EventUiState(isLoading = false, error = e.message ?: "Ошибка загрузки события")
            }
        }
    }

    fun onConfirmFirstPress(userId: String) {
        _uiState.value = _uiState.value.copy(
            confirmFirstPress = _uiState.value.confirmFirstPress + (userId to true)
        )
    }

    fun confirmPayment(eventId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            try {
                eventRepository.confirmEvent(eventId, userId)
                _uiState.value = _uiState.value.copy(
                    confirmFirstPress = _uiState.value.confirmFirstPress - userId
                )
                loadEvent(eventId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка: ${e.message}")
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(linksLoading = false, error = e.message ?: "Ошибка загрузки ссылок")
            }
        }
    }

    fun deleteEvent(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                eventRepository.deleteEvent(currentEventId)
                onDone()
            } catch (_: Exception) {}
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
        val friends = eventRepository.getFriendsList(userId)
        return coroutineScope {
            friends.map { friend ->
                async {
                    try {
                        val user = userRepository.getUser(friend.user_id)
                        friend.copy(photo_url = user.photo_url)
                    } catch (_: Exception) { friend }
                }
            }.awaitAll()
        }
    }

    suspend fun addParticipantSuspend(eventId: String, userId: String) {
        eventRepository.addParticipant(eventId, userId)
    }

    suspend fun addGuestSuspend(eventId: String, name: String) {
        eventRepository.addGuest(eventId, name)
    }

    suspend fun updatePurchaseSuspend(eventId: String, purchaseId: String, request: PurchaseUpdateRequest): PurchaseResponse {
        return eventRepository.updatePurchase(eventId, purchaseId, request)
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
