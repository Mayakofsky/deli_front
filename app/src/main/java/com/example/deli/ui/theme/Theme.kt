package com.example.deli.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// цветовая схема для тёмной темы
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// цветовая схема для светлой темы
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// применяет тему ко всему приложению
@Composable
fun DELITheme(
    // по умолчанию следует системной теме
    darkTheme: Boolean = isSystemInDarkTheme(),

    // разрешает динамические цвета на android 12 и выше
    dynamicColor: Boolean = true,

    // содержимое к которому применяется тема
    content: @Composable () -> Unit
) {
    // выбирает цветовую схему в зависимости от версии и настроек
    val colorScheme = when {

        // использует динамические цвета системы на android 12 и выше
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // использует заданную тёмную схему
        darkTheme -> DarkColorScheme

        // использует заданную светлую схему
        else -> LightColorScheme
    }

    // применяет выбранную схему, типографику и содержимое
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}