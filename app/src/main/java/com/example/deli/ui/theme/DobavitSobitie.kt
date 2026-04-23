package com.example.deli.ui.theme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DobavitSobitie(
    innerPadding: PaddingValues,
    friends: List<FriendRecord>,
    onBack: () -> Unit,
    onCreateSobitie: (Sobitie) -> Unit
) {
    // хранит список участников события
    val participants = remember { mutableStateListOf(Participant()) }

    // хранит общую сумму события
    var totalAmount by remember { mutableStateOf("") }

    // хранит выбранную дату
    var selectedDate by remember { mutableStateOf("") }

    // управляет видимостью календаря
    var showDatePicker by remember { mutableStateOf(false) }

    // хранит выбранное фото
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // управляет видимостью диалога выбора друзей
    var showFriendsDialog by remember { mutableStateOf(false) }

    // открывает галерею для выбора изображения
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    // переводит введенную сумму в число
    val total = totalAmount.toDoubleOrNull() ?: 0.0

    // считает равную долю на каждого участника
    val equalShare = if (participants.isNotEmpty()) total / participants.size else 0.0

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        // показывает диалог выбора даты
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },

            // кнопка подтверждает выбор даты
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        selectedDate = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    // текст кнопки подтверждения
                    Text("OK")
                }
            },

            // кнопка закрывает календарь без выбора
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    // текст кнопки отмены
                    Text("Отмена")
                }
            }
        ) {
            // отображает календарь для выбора даты
            DatePicker(state = datePickerState)
        }
    }

    if (showFriendsDialog) {
        // показывает диалог выбора участников из друзей
        AlertDialog(
            onDismissRequest = { showFriendsDialog = false },

            // заголовок диалога
            title = { Text("Выбрать из друзей") },

            // основное содержимое диалога
            text = {
                if (friends.isEmpty()) {
                    // показывает сообщение если друзей нет
                    Text("У вас пока нет друзей")
                } else {
                    // показывает список друзей для выбора
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(friends) { friend ->
                            // карточка друга добавляет его в участники
                            Card(
                                onClick = {
                                    val newParticipant = Participant(
                                        name = "${friend.user.firstName} ${friend.user.lastName}",
                                        phone = friend.user.phone
                                    )
                                    participants.add(newParticipant)
                                    showFriendsDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                // размещает фото и данные друга в строку
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // контейнер для фото друга или иконки
                                    Surface(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        if (friend.user.photoUri != null) {
                                            // отображает фото друга
                                            Image(
                                                painter = rememberAsyncImagePainter(friend.user.photoUri),
                                                contentDescription = "Фото",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            // показывает иконку если фото нет
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }

                                    // блок с именем и номером телефона
                                    Column {
                                        // показывает имя друга
                                        Text(
                                            text = "${friend.user.firstName} ${friend.user.lastName}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        // показывает номер телефона друга
                                        Text(
                                            text = friend.user.phone,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },

            // кнопка закрывает диалог
            confirmButton = {
                TextButton(onClick = { showFriendsDialog = false }) {
                    // текст кнопки закрытия
                    Text("Закрыть")
                }
            }
        )
    }

    // основной контейнер экрана
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp)
    ) {
        // заголовок экрана
        Text(
            text = "Создание события",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // отступ после заголовка
        Spacer(modifier = Modifier.height(16.dp))

        // список всех блоков формы
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // поле показывает выбранную дату события
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    label = { Text("Дата события") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        // кнопка открывает календарь
                        IconButton(onClick = { showDatePicker = true }) {
                            Text("📅")
                        }
                    }
                )
            }

            item {
                // поле для ввода общей суммы события
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Общая сумма") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // строка с кнопкой выбора фото
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // кнопка открывает галерею для выбора фото
                    FilledTonalButton(onClick = { photoLauncher.launch("image/*") }) {
                        Text("Прикрепить фото")
                    }

                    if (photoUri != null) {
                        // отступ перед сообщением о выбранном фото
                        Spacer(modifier = Modifier.width(12.dp))

                        // показывает что фото успешно выбрано
                        Text(
                            text = "Фото выбрано ✓",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (photoUri != null) {
                item {
                    // показывает превью выбранного изображения
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = "Фото",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                // строка заголовка участников и кнопок добавления
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // заголовок блока участников
                    Text(
                        text = "Участники",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row {
                        // кнопка открывает выбор из друзей
                        IconButton(onClick = { showFriendsDialog = true }) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = "Из друзей",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // кнопка добавляет пустого участника вручную
                        IconButton(onClick = { participants.add(Participant()) }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Добавить",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            itemsIndexed(participants) { index, participant ->
                val extra = participant.extraAmount.toDoubleOrNull() ?: 0.0
                val personalTotal = equalShare + extra

                // карточка с данными одного участника
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    // вертикально размещает поля участника
                    Column(modifier = Modifier.padding(16.dp)) {
                        // строка с номером участника и удалением
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // показывает номер участника
                            Text(
                                text = "Участник ${index + 1}",
                                style = MaterialTheme.typography.titleSmall
                            )

                            if (participants.size > 1) {
                                // кнопка удаляет участника из списка
                                IconButton(onClick = { participants.removeAt(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // поле для ввода имени участника
                        OutlinedTextField(
                            value = participant.name,
                            onValueChange = { newName ->
                                participants[index] = participant.copy(name = newName)
                            },
                            label = { Text("Имя") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // отступ между полями
                        Spacer(modifier = Modifier.height(8.dp))

                        // поле для ввода телефона участника
                        OutlinedTextField(
                            value = participant.phone,
                            onValueChange = { newPhone ->
                                participants[index] = participant.copy(phone = newPhone)
                            },
                            label = { Text("Номер телефона") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // отступ между полями
                        Spacer(modifier = Modifier.height(8.dp))

                        // поле для ввода дополнительной суммы
                        OutlinedTextField(
                            value = participant.extraAmount,
                            onValueChange = { newExtra ->
                                participants[index] = participant.copy(extraAmount = newExtra)
                            },
                            label = { Text("Доп. сумма (необязательно)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // отступ перед расчетом суммы
                        Spacer(modifier = Modifier.height(8.dp))

                        // показывает расчет доли участника
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

            item {
                // карточка показывает итоговую информацию по событию
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    val extraSum = participants.sumOf {
                        it.extraAmount.toDoubleOrNull() ?: 0.0
                    }

                    // блок с итоговыми расчетами
                    Column(modifier = Modifier.padding(16.dp)) {
                        // заголовок итогового блока
                        Text(
                            text = "Итого",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // отступ после заголовка
                        Spacer(modifier = Modifier.height(4.dp))

                        // показывает общую сумму
                        Text(
                            text = "Общая сумма: ${"%.2f".format(total)} ₽",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // показывает равную долю на участника
                        Text(
                            text = "На каждого поровну: ${"%.2f".format(equalShare)} ₽",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // показывает сумму всех доплат
                        Text(
                            text = "Сумма доплат: ${"%.2f".format(extraSum)} ₽",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // показывает количество участников
                        Text(
                            text = "Участников: ${participants.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // отступ перед кнопками внизу
        Spacer(modifier = Modifier.height(12.dp))

        // нижняя строка с действиями
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // кнопка возвращает назад без создания события
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Назад")
            }

            // кнопка создает новое событие
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