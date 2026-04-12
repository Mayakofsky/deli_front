package com.example.deli

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deli.ui.theme.Dolzhnik
import com.example.deli.ui.theme.Sobitie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class MainViewModel : ViewModel() {

    // Состояние списка должников
    private val _dolzhniki = MutableStateFlow<List<Dolzhnik>>(emptyList())
    val dolzhniki: StateFlow<List<Dolzhnik>> = _dolzhniki

    // Состояние списка событий
    private val _sobitiya = MutableStateFlow<List<Sobitie>>(emptyList())
    val sobitiya: StateFlow<List<Sobitie>> = _sobitiya

    // Состояние темы
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    // Добавление должника
    fun addDolzhnik(dolzhnik: Dolzhnik) {
        _dolzhniki.value = _dolzhniki.value + dolzhnik
    }

    // Удаление должника
    fun removeDolzhnik(dolzhnik: Dolzhnik) {
        _dolzhniki.value = _dolzhniki.value - dolzhnik
    }

    // Добавление события
    fun addSobitie(sobitie: Sobitie) {
        _sobitiya.value = _sobitiya.value + sobitie
    }

    // Удаление события
    fun removeSobitie(sobitie: Sobitie) {
        _sobitiya.value = _sobitiya.value - sobitie
    }

    // Переключение темы в памяти
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Сохранение темы в DataStore
    fun saveDarkTheme(context: Context, isDark: Boolean) {
        viewModelScope.launch {
            ThemePreferences.saveDarkTheme(context, isDark)
        }
    }

    // Загрузка темы из DataStore
    fun loadDarkTheme(context: Context) {
        viewModelScope.launch {
            ThemePreferences.isDarkTheme(context).collect { isDark ->
                _isDarkTheme.value = isDark
            }
        }
    }
}