package com.example.deli.viewmodel

import com.example.deli.model.DebtCreateRequest
import com.example.deli.model.FriendUser
import com.example.deli.repository.DebtRepository
import com.example.deli.repository.FriendRepository
import com.example.deli.repository.UserRepository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddDebtUiState(
    val friends: List<FriendUser> = emptyList(),
    val friendsLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val photoUrl: String? = null,
    val currentUserLink: String? = null,
    val linkLoaded: Boolean = false
)

class AddDebtViewModel : ViewModel() {
    private val friendRepository = FriendRepository()
    private val debtRepository = DebtRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(AddDebtUiState())
    val uiState: StateFlow<AddDebtUiState> = _uiState.asStateFlow()

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

    fun uploadPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)
            try {
                val url = userRepository.uploadPhoto(context, uri)
                _uiState.value = _uiState.value.copy(photoUrl = url, isUploading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка загрузки фото: ${e.message}", isUploading = false)
            }
        }
    }

    fun createDebt(creditorId: String, name: String, amount: Double, description: String, deadline: String?, selectedFriendId: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, error = null)
            try {
                val finalDebtorId = if (selectedFriendId != null) {
                    selectedFriendId
                } else {
                    val guest = debtRepository.createGuest(name)
                    guest.user_id
                }
                debtRepository.createDebt(DebtCreateRequest(
                    creditor_id = creditorId,
                    debtor_id = finalDebtorId,
                    amount = amount,
                    description = description.ifBlank { "Долг: $name" },
                    deadline = deadline,
                    photo_url = _uiState.value.photoUrl
                ))
                onDone()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка: ${e.message}", isCreating = false)
            }
        }
    }
}
