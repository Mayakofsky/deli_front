package com.example.deli

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val paymentLink: String = "",
    val paymentLinkLoading: Boolean = true,
    val serverPhotoUrl: String? = null,
    val serverFirstName: String = "",
    val serverLastName: String = "",
    val isUploading: Boolean = false,
    val error: String? = null
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
                    serverPhotoUrl = user.photo_url,
                    serverFirstName = user.first_name,
                    serverLastName = user.last_name ?: ""
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

    fun saveProfilePhoto(userId: String, context: Context, uri: Uri, onUrlReady: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)
            try {
                val url = userRepository.uploadPhoto(context, uri)
                userRepository.updateUser(userId, UserUpdateRequest(photo_url = url))
                _uiState.value = _uiState.value.copy(serverPhotoUrl = url, isUploading = false)
                onUrlReady(url)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUploading = false, error = "Ошибка загрузки: ${e.message}")
            }
        }
    }
}
