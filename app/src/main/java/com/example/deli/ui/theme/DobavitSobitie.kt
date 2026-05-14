package com.example.deli.ui.theme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.PaddingValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DobavitSobitie(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onCreateSobitie: (Sobitie) -> Unit
) {
    // Список участников события
    val participants = remember { mutableStateListOf(Participant()) }

    // Поля формы события
    var totalAmount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Выбор фото из галереи
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    // Расчёт общей суммы
    val total = totalAmount.toDoubleOrNull() ?: 0.0
    val equalShare = if (participants.isNotEmpty()) total / participants.size else 0.0

    if (showDatePicker) {
        // Диалог выбора даты события
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            selectedDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Основной контейнер экрана
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp)
    ) {
        // Заголовок экрана
        Text(
            text = "Создание события",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Отступ после заголовка
        Spacer(modifier = Modifier.height(16.dp))

        // Прокручиваемая форма события
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Поле даты события
            item {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    label = { Text("Дата события") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Text("📅")
                        }
                    }
                )
            }

            // Поле общей суммы
            item {
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Общая сумма") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Блок выбора фото
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalButton(onClick = { photoLauncher.launch("image/*") }) {
                        Text("Прикрепить фото")
                    }

                    if (photoUri != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Фото выбрано ✓",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (photoUri != null) {
                // Превью выбранного фото
                item {
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = "Фото",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Заголовок блока участников
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Участники",
                        style = MaterialTheme.typography.titleLarge
                    )

                    // Кнопка добавления участника
                    IconButton(onClick = { participants.add(Participant()) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Добавить",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            itemsIndexed(participants) { index, participant ->
                val extra = participant.extraAmount.toDoubleOrNull() ?: 0.0
                val personalTotal = equalShare + extra

                // Карточка участника
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Верхняя строка карточки участника
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Участник ${index + 1}",
                                style = MaterialTheme.typography.titleSmall
                            )

                            if (participants.size > 1) {
                                // Кнопка удаления участника
                                IconButton(onClick = { participants.removeAt(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // Поле имени участника
                        OutlinedTextField(
                            value = participant.name,
                            onValueChange = { newName ->
                                participants[index] = participant.copy(name = newName)
                            },
                            label = { Text("Имя") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Отступ между полями
                        Spacer(modifier = Modifier.height(8.dp))

                        // Поле телефона участника
                        OutlinedTextField(
                            value = participant.phone,
                            onValueChange = { newPhone ->
                                participants[index] = participant.copy(phone = newPhone)
                            },
                            label = { Text("Номер телефона") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Отступ между полями
                        Spacer(modifier = Modifier.height(8.dp))

                        // Поле дополнительной суммы
                        OutlinedTextField(
                            value = participant.extraAmount,
                            onValueChange = { newExtra ->
                                participants[index] = participant.copy(extraAmount = newExtra)
                            },
                            label = { Text("Доп. сумма (необязательно)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Отступ перед итогом участника
                        Spacer(modifier = Modifier.height(8.dp))

                        // Итоговая сумма участника
                        Text(
                            text = "Доля: ${"%.2f".format(equalShare)} ₽" +
                                    if (extra > 0) " + ${"%.2f".format(extra)} ₽ = ${"%.2f".format(personalTotal)} ₽"
                                    else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Карточка итогов события
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    val extraSum = participants.sumOf {
                        it.extraAmount.toDoubleOrNull() ?: 0.0
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Итого",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Общая сумма: ${"%.2f".format(total)} ₽",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "На каждого поровну: ${"%.2f".format(equalShare)} ₽",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Сумма доплат: ${"%.2f".format(extraSum)} ₽",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Участников: ${participants.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Отступ перед кнопками
        Spacer(modifier = Modifier.height(12.dp))

        // Нижние кнопки экрана
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Кнопка возврата назад
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Назад")
            }

            // Кнопка создания события
            Button(
                onClick = {
                    val sobitie = Sobitie(
                        date = selectedDate,
                        totalAmount = total,
                        participants = participants.toList()
                    )
                    onCreateSobitie(sobitie)
                    onBack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Создать")
            }
        }
    }
}