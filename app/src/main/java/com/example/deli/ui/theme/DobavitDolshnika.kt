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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DobavitDolshnika(
    innerPadding: PaddingValues,
    friends: List<FriendRecord>,
    onThirdMainScreen: () -> Unit,
    onBack: () -> Unit,
    onAddDolzhnik: (Dolzhnik) -> Unit
) {
    // хранит название долга
    var title by remember { mutableStateOf("") }

    // хранит введенное имя должника
    var name by remember { mutableStateOf("") }

    // хранит введенную сумму долга
    var amount by remember { mutableStateOf("") }

    // хранит выбранную дату дедлайна
    var deadlineText by remember { mutableStateOf("") }

    // управляет видимостью календаря
    var showDatePicker by remember { mutableStateOf(false) }

    // хранит выбранное фото должника
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // управляет видимостью диалога выбора друзей
    var showFriendsDialog by remember { mutableStateOf(false) }

    // открывает галерею для выбора фото
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        deadlineText = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            .format(Date(millis))
                    }
                    showDatePicker = false
                }) {
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

    if (showFriendsDialog) {
        AlertDialog(
            onDismissRequest = { showFriendsDialog = false },
            title = { Text("Выбрать из друзей") },
            text = {
                if (friends.isEmpty()) {
                    Text("У вас пока нет друзей")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(friends) { friend ->
                            // карточка друга подставляет его данные в форму
                            Card(
                                onClick = {
                                    name = "${friend.user.firstName} ${friend.user.lastName}"
                                    photoUri = friend.user.photoUri?.let { Uri.parse(it) }
                                    showFriendsDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        if (friend.user.photoUri != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(friend.user.photoUri),
                                                contentDescription = "Фото",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                    // показывает только имя друга
                                    Text(
                                        text = "${friend.user.firstName} ${friend.user.lastName}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFriendsDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // основной контейнер экрана с отступом от шапки
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(innerPadding)
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // заголовок экрана
        Text(
            text = "Новый должник",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // подзаголовок с подсказкой
        Text(
            text = "Заполните данные о должнике",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // прокручиваемый блок с полями формы
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // кнопка открывает выбор должника из друзей
            FilledTonalButton(
                onClick = { showFriendsDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Выбрать из друзей")
            }

            // поле для ввода названия долга
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название долга") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // поле для ввода имени должника
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя должника") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // поле для ввода суммы долга
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Сумма долга") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("₽") },
                modifier = Modifier.fillMaxWidth()
            )

            // поле показывает выбранную дату дедлайна
            OutlinedTextField(
                value = deadlineText,
                onValueChange = {},
                label = { Text("Дедлайн") },
                readOnly = true,
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Выбрать дату")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // строка с кнопкой выбора фото
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

            if (photoUri != null) {
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

        Spacer(modifier = Modifier.height(12.dp))

        // нижняя строка с кнопками действий
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Отмена")
            }

            // кнопка сохраняет должника с названием долга
            Button(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank()) {
                        onAddDolzhnik(
                            Dolzhnik(
                                title = title,
                                name = name,
                                amount = amount,
                                deadline = deadlineText,
                                photoUri = photoUri?.toString()
                            )
                        )
                        onThirdMainScreen()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Добавить")
            }
        }
    }
}