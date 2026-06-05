package com.example.deli.ui.screens

import com.example.deli.model.EventParticipant
import com.example.deli.network.RetrofitClient
import com.example.deli.viewmodel.AddPurchaseViewModel

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseScreen(
    innerPadding: PaddingValues,
    eventId: String,
    userId: String,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    val addPurchaseViewModel: AddPurchaseViewModel = viewModel()
    val addState by addPurchaseViewModel.uiState.collectAsState()

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val selectedBeneficiaries = remember { mutableStateListOf<String>() }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            photoUri = it
            addPurchaseViewModel.uploadPhoto(context, it)
        }
    }

    LaunchedEffect(eventId) {
        addPurchaseViewModel.loadParticipants(eventId, userId)
        selectedBeneficiaries.clear()
    }

    LaunchedEffect(addState.participants) {
        if (selectedBeneficiaries.isEmpty() && addState.participants.isNotEmpty()) {
            selectedBeneficiaries.addAll(addState.participants.map { it.user_id }.filter { it != userId })
        }
    }

    val displayError = localError ?: addState.error

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(innerPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text("Добавить покупку", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
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

            Text("Разделить на:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(addState.participants.filter { it.user_id != userId }) { p ->
                    val isSelected = selectedBeneficiaries.contains(p.user_id)
                    Card(
                        onClick = {
                            if (isSelected) selectedBeneficiaries.remove(p.user_id)
                            else selectedBeneficiaries.add(p.user_id)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                modifier = Modifier.size(36.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            }
                            Text("${p.first_name} ${p.last_name}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (displayError != null) {
                Text(displayError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedButton(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !addState.isUploading
            ) {
                if (addState.isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (addState.photoUrl != null) "Фото загружено" else "Добавить фото чека")
            }

            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Фото чека",
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
            }

            Button(
                onClick = {
                    if (description.isBlank()) { localError = "Введите описание"; return@Button }
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0) { localError = "Введите сумму"; return@Button }
                    localError = null
                    addPurchaseViewModel.addPurchase(
                        eventId, userId, description, amount.toDouble(),
                        addState.photoUrl, selectedBeneficiaries.toList(), onCreated
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !addState.isLoading
            ) {
                if (addState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Добавить")
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
