package com.example.deli.ui.theme

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.deli.FriendUser
import com.example.deli.RetrofitClient

@Composable
fun DobavitSobitie(
    innerPadding: PaddingValues,
    userId: String,
    onBack: () -> Unit
) {
    val participants = remember { mutableStateListOf<Participant>() }
    var eventName by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var showFriendsDialog by remember { mutableStateOf(false) }

    val friends = remember { mutableStateListOf<FriendUser>() }
    var friendsLoading by remember { mutableStateOf(false) }

    if (showFriendsDialog) {
        LaunchedEffect(showFriendsDialog) {
            friendsLoading = true
            try {
                val items = RetrofitClient.apiService.getFriendsList(userId)
                friends.clear()
                friends.addAll(items)
            } catch (_: Exception) {}
            friendsLoading = false
        }

        AlertDialog(
            onDismissRequest = { showFriendsDialog = false },
            title = { Text("Выбрать из друзей") },
            text = {
                if (friendsLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (friends.isEmpty()) {
                    Text("У вас пока нет друзей")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(friends) { user ->
                            Card(
                                onClick = {
                                    participants.add(Participant(name = "${user.first_name} ${user.last_name}"))
                                    showFriendsDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(innerPadding)
            .padding(24.dp)
    ) {
        Text(
            text = "Создание события",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Название события") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    label = { Text("Дата события") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Add, contentDescription = "Календарь")
                        }
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Общая сумма") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                        IconButton(onClick = { participants.add(Participant()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            itemsIndexed(participants) { index, participant ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Участник ${index + 1}", style = MaterialTheme.typography.titleSmall)
                            if (participants.size > 1) {
                                IconButton(onClick = { participants.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = participant.name,
                            onValueChange = { participants[index] = participant.copy(name = it) },
                            label = { Text("Имя") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = participant.extraAmount,
                            onValueChange = { participants[index] = participant.copy(extraAmount = it) },
                            label = { Text("Доп. сумма (необязательно)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("Назад")
            }
            Button(onClick = { }, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("Создать")
            }
        }
    }
}