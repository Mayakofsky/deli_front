package com.example.deli.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Profile(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    dolzhniki: List<Dolzhnik>,
    sobitiya: List<Sobitie>
) {
    // Основной контейнер экрана
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp)
    ) {
        // Заголовок экрана
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Отступ после заголовка
        Spacer(modifier = Modifier.height(16.dp))

        // Прокручиваемая область профиля
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Карточка статистики
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Статистика",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val totalDebt = dolzhniki.sumOf {
                            it.amount.toDoubleOrNull() ?: 0.0
                        }
                        val totalEvents = sobitiya.size
                        val totalParticipants = sobitiya.sumOf {
                            it.participants.size
                        }

                        Text(
                            text = "Событий: $totalEvents",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Должников: ${dolzhniki.size}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Общая сумма долгов: ${"%.2f".format(totalDebt)} ₽",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Всего участников: $totalParticipants",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                // Карточка настроек
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Настройки",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Строка переключения темы
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDarkTheme) "Тёмная тема" else "Светлая тема",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { onToggleTheme() }
                            )
                        }
                    }
                }
            }
        }

        // Отступ перед кнопкой
        Spacer(modifier = Modifier.height(12.dp))

        // Кнопка возврата назад
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Назад")
        }
    }
}