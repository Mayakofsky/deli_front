package com.example.deli.ui.theme

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.deli.DebtViewModel
import com.example.deli.MainViewModel
import com.example.deli.RetrofitClient

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
    val context = LocalContext.current
    var photoPreviewUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(item.counterparty?.user_id) {
        debtViewModel.loadCounterpartyLink(item.counterparty?.user_id)
    }

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

            Spacer(Modifier.height(24.dp))

            if (!debtState.linkLoading) {
                if (debtState.counterpartyLink != null) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(debtState.counterpartyLink))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Оплатить")
                    }
                } else {
                    Text(
                        text = "Пользователь не прикрепил ссылку для перевода",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    debtViewModel.deleteDebt(item.debt_id) {
                        onDeleted()
                        onBack()
                    }
                },
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

    photoPreviewUrl?.let { url ->
        PhotoViewerDialog(url = url, onDismiss = { photoPreviewUrl = null })
    }
}
