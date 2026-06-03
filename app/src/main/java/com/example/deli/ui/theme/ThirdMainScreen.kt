package com.example.deli.ui.theme

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deli.EventResponse
import com.example.deli.HomeViewModel
import com.example.deli.SummaryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThirdMainScreen(
    innerPadding: PaddingValues,
    userId: String,
    refreshKey: Int = 0,
    onDobavitSobitie: () -> Unit,
    onDobavitDolshnika: () -> Unit,
    onProfile: () -> Unit,
    onFriends: () -> Unit,
    onEventClick: (String) -> Unit,
    onDebtClick: (SummaryItem) -> Unit
) {
    val homeViewModel: HomeViewModel = viewModel()
    val homeState by homeViewModel.uiState.collectAsState()

    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Мне должны", "Я должен", "События")
    var photoPreviewUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId, refreshKey) { homeViewModel.loadData(userId) }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(innerPadding).padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("DELI", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = onProfile, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Person, contentDescription = "Профиль", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionCard(title = "Событие", icon = Icons.Default.Receipt, onClick = onDobavitSobitie, modifier = Modifier.weight(1f))
            QuickActionCard(title = "Должник", icon = Icons.Default.PersonAdd, onClick = onDobavitDolshnika, modifier = Modifier.weight(1f))
            QuickActionCard(title = "Друзья", icon = Icons.Default.Group, onClick = onFriends, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        homeState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        SecondaryTabRow(
            selectedTabIndex = tabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(title, fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (tabIndex == 2) {
            PullToRefreshBox(
                isRefreshing = homeState.isRefreshing,
                onRefresh = { homeViewModel.refresh(userId) },
                modifier = Modifier.fillMaxSize()
            ) {
                if (homeState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (homeState.events.isEmpty()) {
                    EmptyPlaceholder("У вас нет событий")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                        items(homeState.events) { event ->
                            EventCard(event = event, onClick = { onEventClick(event.id) })
                        }
                    }
                }
            }
        } else {
            val items = if (tabIndex == 0) homeState.owedItems else homeState.dueItems
            PullToRefreshBox(
                isRefreshing = homeState.isRefreshing,
                onRefresh = { homeViewModel.refresh(userId) },
                modifier = Modifier.fillMaxSize()
            ) {
                if (homeState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (items.isEmpty()) {
                    EmptyPlaceholder(if (tabIndex == 0) "Вам никто не должен" else "Вы никому не должны")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                        items(items) { item ->
                            SummaryCard(item = item, isOwed = tabIndex == 0, onEventClick = onEventClick, onDebtClick = onDebtClick)
                        }
                    }
                }
            }
        }

        photoPreviewUrl?.let { url ->
            PhotoViewerDialog(url = url, onDismiss = { photoPreviewUrl = null })
        }
    }
}

@Composable
fun SummaryCard(item: SummaryItem, isOwed: Boolean, onEventClick: (String) -> Unit, onDebtClick: (SummaryItem) -> Unit = {}) {
    if (item.type == "debt") {
        DebtSummaryCard(item = item, isOwed = isOwed, onClick = { onDebtClick(item) })
    } else {
        EventSummaryCard(item = item, isOwed = isOwed, onEventClick = onEventClick)
    }
}

@Composable
fun DebtSummaryCard(item: SummaryItem, isOwed: Boolean, onClick: () -> Unit) {
    val person = item.counterparty
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${person?.first_name} ${person?.last_name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${"%.0f".format(item.amount)} руб",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isOwed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun EventSummaryCard(item: SummaryItem, isOwed: Boolean, onEventClick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Событие «${item.event_title}»",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.deadline != null) {
                        Text("До ${item.deadline.take(10)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${"%.0f".format(item.amount)} руб",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isOwed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(24.dp))
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    val people = if (isOwed) item.debtors else item.creditors
                    if (people != null) {
                        people.forEach { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${p.first_name} ${p.last_name}", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${"%.0f".format(if (isOwed) kotlin.math.abs(p.balance) else kotlin.math.abs(p.balance))} руб",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onEventClick(item.event_id ?: "") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Подробнее")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun EventCard(event: EventResponse, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (event.deadline != null) {
                    Text("До ${event.deadline.take(10)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "Участников: ${event.participants?.size ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (event.status == "active") "Активно" else "Закрыто",
                style = MaterialTheme.typography.bodyMedium,
                color = if (event.status == "active") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EmptyPlaceholder(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
