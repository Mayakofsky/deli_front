package com.example.deli.ui.theme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.deli.AddDebtViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DobavitDolshnika(
    innerPadding: PaddingValues,
    userId: String,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    val addDebtViewModel: AddDebtViewModel = viewModel()
    val debtState by addDebtViewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showFriendsDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var selectedFriendId by remember { mutableStateOf<String?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            photoUri = it
            addDebtViewModel.uploadPhoto(context, it)
        }
    }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

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
            addDebtViewModel.loadFriends(userId)
        }

        AlertDialog(
            onDismissRequest = { showFriendsDialog = false },
            title = { Text("Выбрать из друзей") },
            text = {
                if (debtState.friendsLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (debtState.friends.isEmpty()) {
                    Text("У вас пока нет друзей")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(debtState.friends) { user ->
                            Card(
                                onClick = {
                                    selectedFriendId = user.user_id
                                    name = "${user.first_name} ${user.last_name}"
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

    val displayError = localError ?: debtState.error

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(innerPadding).padding(24.dp)
    ) {
        Text(
            text = "Добавить должника",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; selectedFriendId = null },
                label = { Text("Имя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Сумма") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = selectedDate?.let { dateFormat.format(Date(it)) } ?: "",
                onValueChange = {},
                label = { Text("Дата дедлайна (необязательно)") },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Календарь")
                    }
                }
            )

            OutlinedButton(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !debtState.isUploading
            ) {
                if (debtState.isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (debtState.photoUrl != null) "Фото загружено" else "Добавить фото чека")
            }

            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Фото чека",
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Button(
                onClick = { showFriendsDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text("Выбрать из друзей")
            }

            if (displayError != null) {
                Text(displayError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("Назад")
            }
            Button(
                onClick = {
                    if (name.isBlank()) { localError = "Введите имя"; return@Button }
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0) { localError = "Введите корректную сумму"; return@Button }
                    localError = null
                    addDebtViewModel.createDebt(
                        creditorId = userId,
                        name = name,
                        amount = amount.toDouble(),
                        description = description,
                        deadline = selectedDate?.let { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date(it)) },
                        selectedFriendId = selectedFriendId,
                        onDone = onCreated
                    )
                },
                modifier = Modifier.weight(1f).height(52.dp),
                enabled = !debtState.isCreating
            ) {
                if (debtState.isCreating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Добавить")
            }
        }
    }
}
