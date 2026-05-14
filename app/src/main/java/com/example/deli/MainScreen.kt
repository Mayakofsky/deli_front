package com.example.deli

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    innerPadding: PaddingValues,
    onNavigate: () -> Unit
) {
    // Основной контейнер экрана
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Название приложения
        Text(
            text = "DELI",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Отступ между элементами
        Spacer(modifier = Modifier.height(8.dp))

        // Краткое описание приложения
        Text(
            text = "Делим расходы просто",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Отступ перед кнопкой
        Spacer(modifier = Modifier.height(48.dp))

        // Кнопка перехода на следующий экран
        Button(
            onClick = onNavigate,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(text = "Начать")
        }
    }
}