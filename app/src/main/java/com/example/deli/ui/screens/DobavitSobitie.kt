package com.example.deli.ui.screens

import com.example.deli.viewmodel.CreateEventViewModel

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DobavitSobitie(
    innerPadding: PaddingValues,
    userId: String,
    onBack: () -> Unit,
    onCreated: (String) -> Unit
) {
    val createEventViewModel: CreateEventViewModel = viewModel()
    val createState by createEventViewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    val participantUserIds = remember { mutableStateListOf<String>() }
    val participantNames = remember { mutableStateListOf<String>() }
    var showFriendsDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    LaunchedEffect(createState.createdEventId) {
        createState.createdEventId?.let { onCreated(it) }
    }

    LaunchedEffect(userId) {
        createEventViewModel.loadCurrentUser(userId)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showFriendsDialog) {
        LaunchedEffect(showFriendsDialog) {
            createEventViewModel.loadFriends(userId)
        }

        AlertDialog(
            onDismissRequest = { showFriendsDialog = false },
            title = { Text("Выбрать из друзей") },
            text = {
                if (createState.friendsLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (createState.friends.isEmpty()) {
                    Text("У вас пока нет друзей")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(createState.friends) { user ->
                            val alreadyAdded = participantUserIds.contains(user.user_id)
                            Card(
                                onClick = {
                                    if (!alreadyAdded) {
                                        participantUserIds.add(user.user_id)
                                        participantNames.add("${user.first_name} ${user.last_name}")
                                    }
                                    showFriendsDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (alreadyAdded) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp).clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                    Text("${user.first_name} ${user.last_name}", style = MaterialTheme.typography.bodyLarge)
                                    if (alreadyAdded) {
                                        Icon(Icons.Default.Check, contentDescription = "Добавлен", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFriendsDialog = false }) { Text("Закрыть") }
            }
        )
    }

    val displayError = localError ?: createState.error

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(innerPadding).padding(24.dp)
    ) {
        Text(
            text = "Создание события",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (createState.linkLoaded && createState.currentUserLink.isNullOrBlank()) {
            Text(
                text = "Вы не прикрепили ссылку для перевода. Можете прикрепить её в профиле",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название события") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = selectedDate?.let { dateFormat.format(Date(it)) } ?: "",
                    onValueChange = {},
                    label = { Text("Дата события (необязательно)") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Календарь")
                        }
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Участники", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Row {
                        IconButton(onClick = { showFriendsDialog = true }) {
                            Icon(Icons.Default.Group, contentDescription = "Из друзей", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            participantUserIds.add("")
                            participantNames.add("")
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            itemsIndexed(participantNames) { index, name ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { participantNames[index] = it },
                            label = { Text("Участник ${index + 1}") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            participantUserIds.removeAt(index)
                            participantNames.removeAt(index)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        if (displayError != null) {
            Text(displayError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("Назад")
            }
            Button(
                onClick = {
                    if (title.isBlank()) { localError = "Введите название события"; return@Button }
                    localError = null
                    createEventViewModel.createEvent(
                        creatorId = userId,
                        title = title,
                        deadline = selectedDate?.let { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date(it)) },
                        participantIds = participantUserIds.filter { it.isNotEmpty() },
                        guestNames = participantNames.filterIndexed { index, _ -> participantUserIds[index].isEmpty() }
                    )
                },
                modifier = Modifier.weight(1f).height(52.dp),
                enabled = !createState.isCreating
            ) {
                if (createState.isCreating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Создать")
            }
        }
    }
}
