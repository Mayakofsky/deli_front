package com.example.deli

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
class MainViewModel : ViewModel() {

    // хранит текущее состояние темы
    private val _isDarkTheme = MutableStateFlow(false)

    // публичный поток темы только для чтения
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // хранит имя пользователя
    private val _userName = MutableStateFlow("Пользователь")

    // публичный поток имени только для чтения
    val userName: StateFlow<String> = _userName.asStateFlow()

    // хранит id пользователя
    private val _userId = MutableStateFlow("")

    val userId: StateFlow<String> = _userId.asStateFlow()

    fun setUserId(id: String) {
        _userId.value = id
    }

    // хранит ссылку на фото пользователя
    private val _userPhotoUri = MutableStateFlow<String?>(null)

    // публичный поток фото только для чтения
    val userPhotoUri: StateFlow<String?> = _userPhotoUri.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)

    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

    // ключ для сохранения имени в datastore
    private val USER_NAME_KEY = stringPreferencesKey("user_name")

    // ключ для сохранения фото в datastore
    private val USER_PHOTO_KEY = stringPreferencesKey("user_photo")

    private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // загружает сохраненные данные из datastore при запуске
    fun loadDarkTheme(context: Context) {
        viewModelScope.launch {
            val prefs = context.dataStore.data.first()

            // восстанавливает тему или устанавливает светлую по умолчанию
            _isDarkTheme.value = prefs[DARK_THEME_KEY] ?: false

            // восстанавливает имя или устанавливает значение по умолчанию
            _userName.value = prefs[USER_NAME_KEY] ?: "Пользователь"

            // восстанавливает фото пользователя
            _userPhotoUri.value = prefs[USER_PHOTO_KEY]

            // восстанавливает состояние уведомлений
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

    // обновляет имя и фото профиля в памяти и сохраняет в datastore
    fun updateProfile(context: Context, name: String, photoUri: String?) {

        // обновляет имя в потоке
        _userName.value = name

        // обновляет фото в потоке
        _userPhotoUri.value = photoUri

        viewModelScope.launch {
            context.dataStore.edit { prefs ->

                // сохраняет новое имя
                prefs[USER_NAME_KEY] = name

                if (photoUri != null) {
                    // сохраняет новое фото
                    prefs[USER_PHOTO_KEY] = photoUri
                } else {
                    // удаляет фото если оно не выбрано
                    prefs.remove(USER_PHOTO_KEY)
                }
            }
        }
    }
}