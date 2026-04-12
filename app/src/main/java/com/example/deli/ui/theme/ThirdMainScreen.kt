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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThirdMainScreen(
    innerPadding: PaddingValues,
    dolzhniki: List<Dolzhnik>,
    sobitiya: List<Sobitie>,
    onDobavitSobitie: () -> Unit,
    onDobavitDolshnika: () -> Unit,
    onProfile: () -> Unit,
    onDeleteDolzhnik: (Dolzhnik) -> Unit,
    onDeleteSobitie: (Sobitie) -> Unit
) {
    // Состояние выбранного должника для удаления
    var dolzhnikToDelete by remember { mutableStateOf<Dolzhnik?>(null) }

    // Состояние выбранного события для удаления
    var sobitieToDelete by remember { mutableStateOf<Sobitie?>(null) }

    if (dolzhnikToDelete != null) {
        // Диалог подтверждения удаления должника
        AlertDialog(
            onDismissRequest = { dolzhnikToDelete = null },
            title = {
                Text(
                    text = "Удаление должника",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить должника \"${dolzhnikToDelete!!.name}\"?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDolzhnik(dolzhnikToDelete!!)
                        dolzhnikToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dolzhnikToDelete = null }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (sobitieToDelete != null) {
        // Диалог подтверждения удаления события
        AlertDialog(
            onDismissRequest = { sobitieToDelete = null },
            title = {
                Text(
                    text = "Удаление события",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить событие от ${sobitieToDelete!!.date} на сумму ${"%.2f".format(sobitieToDelete!!.totalAmount)} ₽?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSobitie(sobitieToDelete!!)
                        sobitieToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { sobitieToDelete = null }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    // Основной контейнер главного экрана
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок экрана
        Text(
            text = "Главная",
            style = MaterialTheme.typography.headlineMedium
        )

        // Отступ после заголовка
        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка перехода на создание события
        Button(
            onClick = onDobavitSobitie,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сформировать событие")
        }

        // Отступ между кнопками
        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка перехода на добавление должника
        FilledTonalButton(
            onClick = onDobavitDolshnika,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить должника")
        }

        // Отступ между кнопками
        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка перехода в профиль
        OutlinedButton(
            onClick = onProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Профиль")
        }

        // Отступ перед списками
        Spacer(modifier = Modifier.height(24.dp))

        // Прокручиваемый список событий и должников
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок блока событий
            item {
                Text(
                    text = "События",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (sobitiya.isEmpty()) {
                // Текст при отсутствии событий
                item {
                    Text(
                        text = "Пока нет событий",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(sobitiya) { sobitie ->
                    val equalShare = if (sobitie.participants.isNotEmpty()) {
                        sobitie.totalAmount / sobitie.participants.size
                    } else {
                        0.0
                    }

                    // Карточка события
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Верхняя строка карточки события
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Дата: ${sobitie.date}",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                // Кнопка удаления события
                                IconButton(
                                    onClick = { sobitieToDelete = sobitie }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Удалить событие",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            // Основная информация о событии
                            Text(
                                text = "Сумма: ${"%.2f".format(sobitie.totalAmount)} ₽",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Участников: ${sobitie.participants.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "На каждого: ${"%.2f".format(equalShare)} ₽",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Отступ перед участниками
                            Spacer(modifier = Modifier.height(8.dp))

                            // Список участников события
                            sobitie.participants.forEach { participant ->
                                val extra = participant.extraAmount.toDoubleOrNull() ?: 0.0
                                val personalTotal = equalShare + extra

                                Text(
                                    text = "${participant.name}: ${"%.2f".format(personalTotal)} ₽" +
                                            if (extra > 0) " (доп. ${"%.2f".format(extra)} ₽)" else "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Заголовок блока должников
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Должники",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (dolzhniki.isEmpty()) {
                // Текст при отсутствии должников
                item {
                    Text(
                        text = "Пока нет должников",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(dolzhniki) { dolzhnik ->
                    // Карточка должника
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Верхняя строка карточки должника
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dolzhnik.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                // Кнопка удаления должника
                                IconButton(
                                    onClick = { dolzhnikToDelete = dolzhnik }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Удалить должника",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            // Основная информация о должнике
                            Text(
                                text = "Сумма: ${dolzhnik.amount} ₽",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Дедлайн: ${dolzhnik.deadline}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}