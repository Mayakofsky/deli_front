package com.example.deli.viewmodel

import com.example.deli.repository.AuthRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userId: String? = null
)

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, firstName: String, lastName: String) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Заполните все поля")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                val response = authRepository.register(email.trim(), password, firstName.trim(), lastName.trim())
                _uiState.value = AuthUiState(userId = response.user_id)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "Ошибка регистрации: ${e.message}")
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Заполните все поля")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                val response = authRepository.login(email.trim(), password)
                _uiState.value = AuthUiState(userId = response.user_id)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "Ошибка входа: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
