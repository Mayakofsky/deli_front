package com.example.deli.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// настройки типографики приложения
val Typography = Typography(

    // стиль для основного текста
    bodyLarge = TextStyle(
        // стандартное семейство шрифтов
        fontFamily = FontFamily.Default,

        // обычный вес шрифта
        fontWeight = FontWeight.Normal,

        // размер шрифта
        fontSize = 16.sp,

        // высота строки
        lineHeight = 24.sp,

        // межбуквенный интервал
        letterSpacing = 0.5.sp
    )
)