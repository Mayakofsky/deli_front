package com.example.deli.ui.screens

import com.example.deli.model.DebtResponse
import com.example.deli.network.RetrofitClient
import com.example.deli.viewmodel.DebtUiState
import com.example.deli.viewmodel.DebtViewModel
import com.example.deli.viewmodel.MainViewModel

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
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
            val photoUrl = debtState.counterpartyPhotoUrl
            if (photoUrl != null) {
                val fullPhotoUrl = if (photoUrl.startsWith("http")) photoUrl
                    else RetrofitClient.fullUrl(photoUrl)
                Surface(
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    AsyncImage(
                        model = fullPhotoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
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

            if (debtStatus != null) {
                Spacer(Modifier.height(12.dp))
                StatusBadge(status = debtStatus)
            }

            if (!debtDetail?.photo_url.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = RetrofitClient.fullUrl(debtDetail.photo_url),
                        contentDescription = "Фото чека",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
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
                    debtViewModel = debtViewModel,
                    debtState = debtState,
                    onPay = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(debtState.counterpartyLink))
                        context.startActivity(intent)
                    }
                )
            } else {
                CreditorActions(
                    debtId = item.debt_id,
                    debtDetail = debtDetail,
                    debtViewModel = debtViewModel,
                    onClosed = {
                        onDeleted()
                        onBack()
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (text, color) = when (status) {
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
    debtViewModel: DebtViewModel,
    debtState: DebtUiState,
    onPay: () -> Unit
) {
    val status = debtDetail?.status

    if (status == "paid") {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
        return
    }

    if (debtDetail?.payment_photo_url == "confirmed") {
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
            text = "Подтверждено",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            textAlign = TextAlign.Center
        )
        return
    }

    if (!debtState.linkLoading) {
        if (!debtState.counterpartyLink.isNullOrBlank()) {
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

    if (debtState.debtorFirstPress) {
        Button(
            onClick = {
                if (debtId != null) debtViewModel.confirmTransfer(debtId)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Text("Нажмите еще раз для подтверждения")
        }
    } else {
        Button(
            onClick = { debtViewModel.onDebtorFirstPress() },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Подтвердить перевод")
        }
    }
}

@Composable
private fun CreditorActions(
    debtId: String?,
    debtDetail: DebtResponse?,
    debtViewModel: DebtViewModel,
    onClosed: () -> Unit
) {
    val status = debtDetail?.status
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val pendingDelete = {
        showDeleteConfirm = true
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить долг") },
            text = { Text("Вы уверены? Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        debtViewModel.closeDebt(debtId) { onClosed() }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
            }
        )
    }

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
            Spacer(Modifier.height(16.dp))
            DeleteDebtButton(onClick = pendingDelete)
        }
        else -> {
            if (debtDetail?.payment_photo_url == "confirmed") {
                Text(
                    text = "Должник подтвердил перевод",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )

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
                    onClick = { pendingDelete() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Подтвердить и удалить долг")
                }
            } else {
                Text(
                    text = "Ожидается оплата от должника",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                DeleteDebtButton(onClick = pendingDelete)
            }
        }
    }
}

@Composable
private fun DeleteDebtButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(Icons.Default.Delete, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Удалить долг")
    }
}


