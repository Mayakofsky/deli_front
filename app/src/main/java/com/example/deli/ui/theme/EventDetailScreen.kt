package com.example.deli.ui.theme

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.deli.BalanceItem
import com.example.deli.EventParticipant
import com.example.deli.EventResponse
import com.example.deli.GuestCreateRequest
import com.example.deli.ParticipantAddRequest
import com.example.deli.PurchaseResponse
import com.example.deli.PurchaseUpdateRequest
import com.example.deli.RetrofitClient

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun EventDetailScreen(
    innerPadding: PaddingValues,
    eventId: String,
    userId: String,
    refreshKey: Int = 0,
    onBack: () -> Unit,
    onAddPurchase: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var event by remember { mutableStateOf<EventResponse?>(null) }
    var purchases by remember { mutableStateOf<List<PurchaseResponse>>(emptyList()) }
    var balances by remember { mutableStateOf<List<BalanceItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var participantsExpanded by remember { mutableStateOf(false) }
    var showAddParticipantDialog by remember { mutableStateOf(false) }
    var showCloseConfirm by remember { mutableStateOf(false) }
    var showDeleteParticipantConfirm by remember { mutableStateOf<String?>(null) }
    var editingPurchase by remember { mutableStateOf<PurchaseResponse?>(null) }
    var photoPreviewUrl by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var participantLinks by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }
    var linksLoading by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            try {
                event = RetrofitClient.apiService.getEvent(eventId)
                purchases = RetrofitClient.apiService.listPurchases(eventId)
                balances = RetrofitClient.apiService.getBalances(eventId)
            } catch (_: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(eventId, refreshKey) { loadData() }

    val buyerIds = purchases.map { it.buyer?.user_id }.toSet()
    val participants = event?.participants?.sortedByDescending { buyerIds.contains(it.user_id) } ?: emptyList()
    val balanceMap = balances.associateBy { it.user_id }

    LaunchedEffect(participantsExpanded, balances) {
        if (!participantsExpanded) return@LaunchedEffect
        val creditorIds = balances.filter { it.balance > 0 }.map { it.user_id }
        if (creditorIds.isEmpty()) return@LaunchedEffect
        linksLoading = true
        participantLinks = coroutineScope {
            creditorIds.map { id ->
                async {
                    try {
                        val user = RetrofitClient.apiService.getUser(id)
                        id to user.link
                    } catch (_: Exception) {
                        id to null
                    }
                }
            }.awaitAll().toMap()
        }
        linksLoading = false
    }

    if (showAddParticipantDialog) {
        AddParticipantDialog(
            eventId = eventId,
            onDismiss = { showAddParticipantDialog = false },
            onAdded = { showAddParticipantDialog = false; loadData() }
        )
    }

    if (showCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm = false },
            title = { Text("Закрыть событие") },
            text = { Text("После закрытия события в него нельзя будет добавлять покупки.") },
            confirmButton = {
                Button(onClick = {
                    showCloseConfirm = false
                    scope.launch {
                        try { RetrofitClient.apiService.closeEvent(eventId) } catch (_: Exception) {}
                        loadData()
                    }
                }) { Text("Закрыть") }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirm = false }) { Text("Отмена") }
            }
        )
    }

    showDeleteParticipantConfirm?.let { uid ->
        AlertDialog(
            onDismissRequest = { showDeleteParticipantConfirm = null },
            title = { Text("Удалить участника?") },
            text = { Text("У этого участника нет долгов, его можно удалить.") },
            confirmButton = {
                Button(onClick = {
                    val id = uid
                    showDeleteParticipantConfirm = null
                    scope.launch {
                        try { RetrofitClient.apiService.removeParticipant(eventId, id) } catch (_: Exception) {}
                        loadData()
                    }
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteParticipantConfirm = null }) { Text("Отмена") }
            }
        )
    }

    editingPurchase?.let { purchase ->
        EditPurchaseDialog(
            purchase = purchase,
            participants = participants,
            onDismiss = { editingPurchase = null },
            onSaved = { editingPurchase = null; loadData() }
        )
    }

    photoPreviewUrl?.let { url ->
        com.example.deli.ui.theme.PhotoViewerDialog(
            url = url,
            onDismiss = { photoPreviewUrl = null }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(innerPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Text(
                text = event?.title ?: "Загрузка...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (event?.status == "active") {
                    IconButton(onClick = { showCloseConfirm = true }) {
                        Icon(Icons.Default.Lock, contentDescription = "Закрыть событие", tint = MaterialTheme.colorScheme.error)
                    }
            }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        onClick = { participantsExpanded = !participantsExpanded },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (participantsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Участники (${participants.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(onClick = { showAddParticipantDialog = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "Добавить", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            if (participantsExpanded) {
                                Spacer(Modifier.height(8.dp))
                                participants.forEach { p ->
                                    val isBuyer = buyerIds.contains(p.user_id)
                                    val bal = balanceMap[p.user_id]
                                    val isZeroBalance = bal == null || kotlin.math.abs(bal.balance) < 0.01
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                modifier = Modifier.size(28.dp).clip(CircleShape),
                                                color = if (isBuyer) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    if (isBuyer) {
                                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                                    } else {
                                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "${p.first_name} ${p.last_name}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isBuyer) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                        Row {
                                            if (bal != null && bal.balance != 0.0) {
                                                Text(
                                                    "${"%.0f".format(bal.balance)} руб",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (bal.balance > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (bal.balance > 0 && p.user_id != userId) {
                                                    Spacer(Modifier.width(8.dp))
                                                    val link = participantLinks[p.user_id]
                                                    if (link != null) {
                                                        Button(
                                                            onClick = {
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                                                context.startActivity(intent)
                                                            },
                                                            modifier = Modifier.height(28.dp),
                                                            shape = RoundedCornerShape(8.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                        ) {
                                                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            Spacer(Modifier.width(4.dp))
                                                            Text("Оплатить", fontSize = 12.sp)
                                                        }
                                                    } else if (!linksLoading) {
                                                        Text(
                                                            "нет ссылки",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }
                                            }
                                            if (isZeroBalance) {
                                                Spacer(Modifier.width(4.dp))
                                                IconButton(onClick = { showDeleteParticipantConfirm = p.user_id }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Удалить", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider()
                    Text("Покупки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                if (purchases.isEmpty()) {
                    item {
                        Text("Пока нет покупок", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(purchases) { p ->
                        PurchaseCard(
                            purchase = p,
                            onEdit = { editingPurchase = p },
                            onPhotoClick = { url -> photoPreviewUrl = url }
                        )
                    }
                }

                item { Spacer(Modifier.height(60.dp)) }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { onAddPurchase(eventId) },
            modifier = Modifier.padding(16.dp).statusBarsPadding()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить покупку")
        }
    }
}

@Composable
fun PurchaseCard(purchase: PurchaseResponse, onEdit: () -> Unit, onPhotoClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(purchase.description, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Купил(а): ${purchase.buyer?.first_name} ${purchase.buyer?.last_name}", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${"%.0f".format(purchase.amount)} руб",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать", modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (!purchase.beneficiaries.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Разделено на:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                purchase.beneficiaries.forEach { b ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("  • ${b.first_name} ${b.last_name}", style = MaterialTheme.typography.bodySmall)
                        Text("${"%.0f".format(b.share_amount)} руб", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (purchase.receipt_photo_url != null) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = RetrofitClient.BASE_URL + purchase.receipt_photo_url.removePrefix("/"),
                        contentDescription = "Фото чека",
                        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                            detectTapGestures { onPhotoClick(purchase.receipt_photo_url) }
                        },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun AddParticipantDialog(
    eventId: String,
    onDismiss: () -> Unit,
    onAdded: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf("menu") }
    val friends = remember { mutableStateListOf<EventParticipant>() }
    var friendsLoading by remember { mutableStateOf(false) }
    var manualName by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (mode == "menu") "Добавить участника" else if (mode == "friends") "Выбрать из друзей" else "Добавить вручную")
        },
        text = {
            when (mode) {
                "menu" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            friendsLoading = true
                            scope.launch {
                                try {
                                    val raw = RetrofitClient.apiService.getFriendsList("")
                                    friends.clear()
                                } catch (_: Exception) {}
                                try {
                                    val list = RetrofitClient.apiService.getFriendsList("")
                                    friends.clear()
                                    friends.addAll(list.map { EventParticipant(it.user_id, it.first_name, it.last_name) })
                                } catch (_: Exception) {}
                                friendsLoading = false
                            }
                            mode = "friends"
                        }, modifier = Modifier.fillMaxWidth()) { Text("Из друзей") }
                        Button(onClick = { mode = "manual" }, modifier = Modifier.fillMaxWidth()) { Text("Добавить вручную") }
                    }
                }
                "friends" -> {
                    if (friendsLoading) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (friends.isEmpty()) {
                        Text("У вас нет друзей")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(friends) { f ->
                                TextButton(
                                    onClick = {
                                        adding = true
                                        scope.launch {
                                            try {
                                                RetrofitClient.apiService.addParticipant(eventId, ParticipantAddRequest(f.user_id))
                                                onAdded()
                                            } catch (e: Exception) { error = e.message }
                                            adding = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !adding
                                ) { Text("${f.first_name} ${f.last_name}", modifier = Modifier.fillMaxWidth()) }
                            }
                        }
                    }
                }
                "manual" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = manualName,
                            onValueChange = { manualName = it },
                            label = { Text("Имя") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (error != null) {
                            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = {
                                if (manualName.isBlank()) return@Button
                                adding = true
                                scope.launch {
                                    try {
                                        RetrofitClient.apiService.addGuest(eventId, GuestCreateRequest(manualName))
                                        onAdded()
                                    } catch (e: Exception) { error = e.message }
                                    adding = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !adding && manualName.isNotBlank()
                        ) { Text("Добавить") }
                    }
                }
            }
        },
        confirmButton = {
            if (mode != "menu") {
                TextButton(onClick = { mode = "menu" }) { Text("Назад") }
            }
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

@Composable
fun EditPurchaseDialog(
    purchase: PurchaseResponse,
    participants: List<EventParticipant>,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var editDescription by remember { mutableStateOf(purchase.description) }
    var editAmount by remember { mutableStateOf(purchase.amount.toString()) }
    val editBeneficiaries = remember {
        mutableStateListOf<String>().apply {
            addAll(purchase.beneficiaries?.map { it.user_id } ?: emptyList())
        }
    }
    var saving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать покупку") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Описание") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("Сумма") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { Text("Разделить на:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) }
                items(participants.filter { it.user_id != purchase.buyer?.user_id }) { p ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = editBeneficiaries.contains(p.user_id),
                            onCheckedChange = { checked ->
                                if (checked) editBeneficiaries.add(p.user_id)
                                else editBeneficiaries.remove(p.user_id)
                            }
                        )
                        Text("${p.first_name} ${p.last_name}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = editAmount.toDoubleOrNull() ?: return@Button
                    saving = true
                    scope.launch {
                        try {
                            RetrofitClient.apiService.updatePurchase(
                                purchase.event_id ?: "",
                                purchase.id,
                                PurchaseUpdateRequest(
                                    description = editDescription,
                                    amount = amount,
                                    beneficiary_ids = editBeneficiaries.toList()
                                )
                            )
                            onSaved()
                        } catch (_: Exception) {}
                        saving = false
                    }
                },
                enabled = !saving && editAmount.toDoubleOrNull() != null
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}


