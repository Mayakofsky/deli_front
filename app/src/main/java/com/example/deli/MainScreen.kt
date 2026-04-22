package com.example.deli

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    innerPadding: PaddingValues,
    isDarkTheme: Boolean,
    onNavigate: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    var isFirstLaunch by remember {
        mutableStateOf(prefs.getBoolean("is_first_launch", true))
    }

    var visible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = tween(1000),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        visible = true

        if (!isFirstLaunch) {
            delay(2000)
            visible = false
            delay(800)
            onNavigate()
        }
    }

    val gradientColors = if (isDarkTheme) {
        listOf(
            Color(0xFF1C1B1F),
            Color(0xFF2B2930),
            Color(0xFF1C1B1F)
        )
    } else {
        listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF5F5F5),
            Color(0xFFE0E0E0)
        )
    }

    val textColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF333333)
    val shadowColor = if (isDarkTheme) Color(0xFF000000) else Color(0xFFBDBDBD)
    val subtitleColor = if (isDarkTheme) Color(0xFFBDBDBD) else Color(0xFF757575)

    // основной контейнер экрана с фоном и выравниванием по центру
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        // вертикально размещает элементы по центру
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // плавно показывает и скрывает заголовок
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1500)),
                exit = fadeOut(animationSpec = tween(800))
            ) {
                // отображает название приложения с тенью и анимацией масштаба
                Text(
                    text = "DELI",
                    style = TextStyle(
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        shadow = Shadow(
                            color = shadowColor,
                            offset = Offset(0f, 4f),
                            blurRadius = 16f
                        )
                    ),
                    modifier = Modifier.scale(scale)
                )
            }

            // создает отступ между заголовком и подзаголовком
            Spacer(modifier = Modifier.height(16.dp))

            // плавно показывает и скрывает подзаголовок
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(2000)),
                exit = fadeOut(animationSpec = tween(800))
            ) {
                // отображает короткое описание приложения
                Text(
                    text = "Делим расходы просто",
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = subtitleColor
                    )
                )
            }

            // создает отступ перед кнопкой
            Spacer(modifier = Modifier.height(48.dp))

            if (isFirstLaunch) {
                // показывает кнопку только при первом запуске
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(2500)),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    // кнопка сохраняет первый запуск и выполняет переход дальше
                    Button(
                        onClick = {
                            prefs.edit()
                                .putBoolean("is_first_launch", false)
                                .apply()

                            isFirstLaunch = false
                            visible = false

                            scope.launch {
                                delay(500)
                                onNavigate()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        // текст внутри кнопки
                        Text(
                            text = "Начать",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}