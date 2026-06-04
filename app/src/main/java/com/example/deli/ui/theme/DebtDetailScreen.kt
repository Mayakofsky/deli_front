package com.example.deli.ui.theme

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.deli.DebtViewModel
import com.example.deli.MainViewModel
import com.example.deli.RetrofitClient
import com.example.deli.DebtResponse
import com.example.deli.EventParticipant

@Composable
fun DebtDetailScreen(
    innerPadding: PaddingValues,
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val selectedDebtItem by mainViewModel.selectedDebtItem.collectAsState()
    val item = selectedDebtItem ?: run {
        onBack()
        return
    }

    val debtViewModel: DebtViewModel = viewModel()
    val debtState by debtViewModel.uiState.collectAsState()
    val isDebtor by mainViewModel.isDebtor.collectAsState()
    val context = LocalContext.current
    var photoPreviewUrl by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && item.debt_id != null) {
            debtViewModel.submitPaymentProof(item.debt_id, context, uri)
        }
    }

    LaunchedEffect(item.counterparty?.user_id) {
        debtViewModel.loadCounterpartyLink(item.counterparty?.user_id)
    }

    LaunchedEffect(item.debt_id) {
        if (item.debt_id != null) {
            debtViewModel.loadDebtDetail(item.debt_id)
        }
    }

    val debtDetail = debtState.debtDetail
    val debtStatus = debtDetail?.status

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
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${item.counterparty?.first_name} ${item.counterparty?.last_name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "${"%.0f".format(item.amount)} руб",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (item.deadline != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "До ${item.deadline.take(10)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (!item.description.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.photo_url != null) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 400.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = if (item.photo_url.startsWith("http")) item.photo_url
                        else RetrofitClient.fullUrl(item.photo_url)
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Фото",
                        modifier = Modifier.fillMaxSize().clickable { photoPreviewUrl = photoUrl },
                        contentScale = ContentScale.Fit
                    )
                }
            }

            if (debtStatus != null) {
                Spacer(Modifier.height(12.dp))
                StatusBadge(status = debtStatus)
            }

            Spacer(Modifier.height(16.dp))

            debtState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            if (isDebtor) {
                DebtorActions(
                    debtId = item.debt_id,
                    debtDetail = debtDetail,
                    debtState = debtState,
                    onPay = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(debtState.counterpartyLink))
                        context.startActivity(intent)
                    },
                    onAttachProof = { photoPickerLauncher.launch("image/*") }
                )
            } else {
                CreditorActions(
                    debtId = item.debt_id,
                    debtDetail = debtDetail,
                    debtViewModel = debtViewModel,
                    onPhotoClick = { url -> photoPreviewUrl = url }
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Удалить долг")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление долга") },
            text = { Text("Вы уверены, что хотите удалить этот долг?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    debtViewModel.deleteDebt(item.debt_id) {
                        onDeleted()
                        onBack()
                    }
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    photoPreviewUrl?.let { url ->
        PhotoViewerDialog(url = url, onDismiss = { photoPreviewUrl = null })
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (text, color) = when (status) {
        "awaiting_confirmation" -> "Ожидает подтверждения" to MaterialTheme.colorScheme.tertiary
        "paid" -> "Долг погашен" to MaterialTheme.colorScheme.primary
        else -> "Активен" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun DebtorActions(
    debtId: String?,
    debtDetail: DebtResponse?,
    debtState: com.example.deli.DebtUiState,
    onPay: () -> Unit,
    onAttachProof: () -> Unit
) {
    val status = debtDetail?.status

    when (status) {
        "paid" -> {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        "awaiting_confirmation" -> {
            Text(
                text = "Подтверждение отправлено. Ожидайте проверки.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
            if (debtDetail.payment_photo_url != null) {
                Spacer(Modifier.height(8.dp))
                PaymentPhotoThumbnail(photoUrl = debtDetail.payment_photo_url)
            }
        }
        else -> {
            if (!debtState.linkLoading) {
                if (debtState.counterpartyLink != null) {
                    Button(
                        onClick = onPay,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Оплатить")
                    }
                    Spacer(Modifier.height(12.dp))
                } else {
                    Text(
                        text = "Пользователь не прикрепил ссылку для перевода",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (debtState.isUploading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                Spacer(Modifier.height(8.dp))
            } else {
                Button(
                    onClick = onAttachProof,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Прикрепить подтверждение оплаты")
                }
            }
        }
    }
}

@Composable
private fun CreditorActions(
    debtId: String?,
    debtDetail: DebtResponse?,
    debtViewModel: DebtViewModel,
    onPhotoClick: (String) -> Unit
) {
    val status = debtDetail?.status

    when (status) {
        "paid" -> {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Долг полностью погашен",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        "awaiting_confirmation" -> {
            Text(
                text = "Должник отправил подтверждение оплаты",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            if (debtDetail.payment_photo_url != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Фото подтверждения:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                PaymentPhotoThumbnail(
                    photoUrl = debtDetail.payment_photo_url,
                    onClick = { onPhotoClick(debtDetail.payment_photo_url) }
                )
            }

            if (debtDetail.debtor != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Плательщик: ${debtDetail.debtor.first_name} ${debtDetail.debtor.last_name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { if (debtId != null) debtViewModel.confirmPayment(debtId) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Подтвердить погашение долга")
            }
        }
        else -> {
            Text(
                text = "Ожидается оплата от должника",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PaymentPhotoThumbnail(
    photoUrl: String,
    onClick: (() -> Unit)? = null
) {
    val fullUrl = if (photoUrl.startsWith("http")) photoUrl
        else RetrofitClient.fullUrl(photoUrl)

    Box(
        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 300.dp)
            .clip(MaterialTheme.shapes.medium)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = fullUrl,
            contentDescription = "Подтверждение оплаты",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
