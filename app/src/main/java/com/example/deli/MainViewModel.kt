package com.example.deli

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _userName = MutableStateFlow("Пользователь")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userPhotoUri = MutableStateFlow<String?>(null)
    val userPhotoUri: StateFlow<String?> = _userPhotoUri.asStateFlow()

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_PHOTO_KEY = stringPreferencesKey("user_photo")

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun loadDarkTheme(context: Context) {
        viewModelScope.launch {
            val prefs = context.dataStore.data.first()
            _isDarkTheme.value = prefs[DARK_THEME_KEY] ?: false
            _userName.value = prefs[USER_NAME_KEY] ?: "Пользователь"
            _userPhotoUri.value = prefs[USER_PHOTO_KEY]
        }
    }

    fun saveDarkTheme(context: Context, value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[DARK_THEME_KEY] = value
            }
        }
    }

    fun updateProfile(context: Context, name: String, photoUri: String?) {
        _userName.value = name
        _userPhotoUri.value = photoUri

        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[USER_NAME_KEY] = name
                if (photoUri != null) {
                    prefs[USER_PHOTO_KEY] = photoUri
                } else {
                    prefs.remove(USER_PHOTO_KEY)
                }
            }
        }
    }
}