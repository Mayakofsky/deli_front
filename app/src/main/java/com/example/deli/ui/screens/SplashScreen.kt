package com.example.deli.ui.screens

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    innerPadding: PaddingValues,
    onNavigate: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // читает SharedPreferences для определения первого запуска
    val prefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    // true если приложение запускается впервые
    var isFirstLaunch by remember {
        mutableStateOf(prefs.getBoolean("is_first_launch", true))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // управляет видимостью элементов для анимации
    var visible by remember { mutableStateOf(false) }

    // анимирует масштаб от 0.5 до 1.0
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = tween(1000),
        label = "scale"
    )

    // запускает анимацию и автопереход если не первый запуск
    LaunchedEffect(Unit) {
        visible = true
        if (!isFirstLaunch) {
            delay(2000)
            visible = false
            delay(800)
            onNavigate()
        }
    }

    // основной контейнер экрана с фоном и выравниванием по центру
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
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
                // отображает название приложения с анимацией масштаба
                Text(
                    text = "DELI",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.scale(scale)
                )
            }

            // отступ между заголовком и подзаголовком
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
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // отступ перед кнопкой
            Spacer(modifier = Modifier.height(48.dp))

            if (isFirstLaunch) {
                // показывает кнопку только при первом запуске
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(2500)),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    // кнопка сохраняет первый запуск и выполняет переход
                    Button(
                        onClick = {
                            prefs.edit()
                                .putBoolean("is_first_launch", false)
                                .apply()
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
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}