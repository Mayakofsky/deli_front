package com.example.deli.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deli.model.SummaryItem
import com.example.deli.util.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
class MainViewModel : ViewModel() {

    private val _selectedDebtItem = MutableStateFlow<SummaryItem?>(null)
    val selectedDebtItem: StateFlow<SummaryItem?> = _selectedDebtItem.asStateFlow()

    private val _isDebtor = MutableStateFlow(false)
    val isDebtor: StateFlow<Boolean> = _isDebtor.asStateFlow()

    fun setSelectedDebtItem(item: SummaryItem?, isDebtor: Boolean = false) {
        _selectedDebtItem.value = item
        _isDebtor.value = isDebtor
    }

    // хранит текущее состояние темы
    private val _isDarkTheme = MutableStateFlow(false)

    // публичный поток темы только для чтения
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // хранит id пользователя
    private val _userId = MutableStateFlow("")

    val userId: StateFlow<String> = _userId.asStateFlow()

    fun setUserId(id: String) {
        _userId.value = id
    }

    // хранит ссылку на фото пользователя (серверный URL)
    private val _userPhotoUri = MutableStateFlow<String?>(null)

    // публичный поток фото только для чтения
    val userPhotoUri: StateFlow<String?> = _userPhotoUri.asStateFlow()

    fun setUserPhotoUri(uri: String?) {
        _userPhotoUri.value = uri
    }

    private val _notificationsEnabled = MutableStateFlow(true)

    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

    private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun loadDarkTheme(context: Context) {
        viewModelScope.launch {
            val prefs = context.dataStore.data.first()

            _isDarkTheme.value = prefs[DARK_THEME_KEY] ?: false
            _notificationsEnabled.value = prefs[NOTIFICATIONS_ENABLED_KEY] ?: true
        }
    }

    fun saveNotificationsEnabled(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[NOTIFICATIONS_ENABLED_KEY] = value
            }
        }
    }

    // сохраняет выбранную тему в datastore
    fun saveDarkTheme(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[DARK_THEME_KEY] = value
            }
        }
    }

    fun updateProfilePhoto(photoUrl: String?) {
        _userPhotoUri.value = photoUrl
    }
}