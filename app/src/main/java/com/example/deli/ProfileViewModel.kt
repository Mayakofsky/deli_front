package com.example.deli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val paymentLink: String = "",
    val paymentLinkLoading: Boolean = true,
    val serverName: String? = null
)

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(paymentLinkLoading = true)
            try {
                val user = userRepository.getUser(userId)
                _uiState.value = ProfileUiState(
                    paymentLink = user.link ?: "",
                    paymentLinkLoading = false,
                    serverName = listOfNotNull(user.first_name, user.last_name).joinToString(" ").ifBlank { null }
                )
            } catch (_: Exception) {
                _uiState.value = ProfileUiState(paymentLinkLoading = false)
            }
        }
    }

    fun savePaymentLink(userId: String, link: String) {
        viewModelScope.launch {
            try {
                userRepository.updateUser(userId, UserUpdateRequest(link = link))
                _uiState.value = _uiState.value.copy(paymentLink = link)
            } catch (_: Exception) {}
        }
    }

    fun saveProfileName(userId: String, name: String) {
        viewModelScope.launch {
            try {
                userRepository.updateUser(userId, UserUpdateRequest(first_name = name))
            } catch (_: Exception) {}
        }
    }
}
