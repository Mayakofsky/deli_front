package com.example.deli.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThirdMainScreen(
    innerPadding: PaddingValues,
    dolzhniki: List<Dolzhnik>,
    sobitiya: List<Sobitie>,
    onDobavitSobitie: () -> Unit,
    onDobavitDolshnika: () -> Unit,
    onProfile: () -> Unit,
    onFriends: () -> Unit,
    onDeleteDolzhnik: (Dolzhnik) -> Unit,
    onDeleteSobitie: (Sobitie) -> Unit,
    onPayDolzhnik: (Dolzhnik) -> Unit,
    onPaySobitie: (Sobitie) -> Unit
) {
    var dolzhnikToDelete by remember { mutableStateOf<Dolzhnik?>(null) }
    var sobitieToDelete by remember { mutableStateOf<Sobitie?>(null) }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("События", "Должники")

    if (dolzhnikToDelete != null) {
        // показывает диалог подтверждения удаления должника
        AlertDialog(
            onDismissRequest = { dolzhnikToDelete = null },
            title = { Text("Удаление") },
            text = { Text("Удалить должника \"${dolzhnikToDelete!!.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteDolzhnik(dolzhnikToDelete!!)
                    dolzhnikToDelete = null
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { dolzhnikToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (sobitieToDelete != null) {
        // показывает диалог подтверждения удаления события
        AlertDialog(
            onDismissRequest = { sobitieToDelete = null },
            title = { Text("Удаление") },
            text = { Text("Удалить событие \"${sobitieToDelete!!.name.ifBlank { sobitieToDelete!!.date }}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteSobitie(sobitieToDelete!!)
                    sobitieToDelete = null
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { sobitieToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // основной вертикальный контейнер экрана с отступом от шапки
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // верхняя строка с логотипом и кнопкой профиля
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // отображает название приложения
                Text(
                    text = "DELI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // кнопка открывает экран профиля
            IconButton(
                onClick = onProfile,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Профиль",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // строка с быстрыми действиями
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionCard(
                title = "Событие",
                icon = Icons.Default.Receipt,
                onClick = onDobavitSobitie,
                modifier = Modifier.weight(1f),
                isPrimary = false
            )
            QuickActionCard(
                title = "Должник",
                icon = Icons.Default.PersonAdd,
                onClick = onDobavitDolshnika,
                modifier = Modifier.weight(1f),
                isPrimary = false
            )
            QuickActionCard(
                title = "Друзья",
                icon = Icons.Default.Group,
                onClick = onFriends,
                modifier = Modifier.weight(1f),
                isPrimary = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // строка вкладок для переключения между событиями и должниками
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (pagerState.currentPage == index)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // контейнер с перелистыванием страниц
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    if (sobitiya.isEmpty()) {
                        EmptyPlaceholder("Пока нет событий")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(sobitiya) { sobitie ->
                                SobitieCard(
                                    sobitie = sobitie,
                                    onDelete = { sobitieToDelete = sobitie },
                                    onPay = { onPaySobitie(sobitie) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (dolzhniki.isEmpty()) {
                        EmptyPlaceholder("Пока нет должников")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(dolzhniki) { dolzhnik ->
                                DolzhnikCard(
                                    dolzhnik = dolzhnik,
                                    onDelete = { dolzhnikToDelete = dolzhnik },
                                    onPay = { onPayDolzhnik(dolzhnik) }
                                )
                            }
                        }
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
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isPrimary)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun EmptyPlaceholder(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SobitieCard(
    sobitie: Sobitie,
    onDelete: () -> Unit,
    onPay: () -> Unit
) {
    val equalShare = if (sobitie.participants.isNotEmpty()) {
        sobitie.totalAmount / sobitie.participants.size
    } else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // показывает название события или дату если название пустое
                    Text(
                        text = sobitie.name.ifBlank { sobitie.date.ifBlank { "Без названия" } },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // показывает дату если есть название
                    if (sobitie.name.isNotBlank() && sobitie.date.isNotBlank()) {
                        Text(
                            text = sobitie.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // показывает количество участников
                    Text(
                        text = "${sobitie.participants.size} участников",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // показывает общую сумму события
                Text(
                    text = "${"%.0f".format(sobitie.totalAmount)} ₽",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // показывает среднюю сумму на одного участника
            Text(
                text = "На каждого: ${"%.2f".format(equalShare)} ₽",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onPay,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Оплатить")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun DolzhnikCard(
    dolzhnik: Dolzhnik,
    onDelete: () -> Unit,
    onPay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // контейнер для фото или стандартной иконки
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        if (dolzhnik.photoUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(dolzhnik.photoUri),
                                contentDescription = "Фото",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Column {
                        // показывает название долга если есть
                        if (dolzhnik.title.isNotBlank()) {
                            Text(
                                text = dolzhnik.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // отображает имя должника
                        Text(
                            text = dolzhnik.name,
                            style = if (dolzhnik.title.isNotBlank())
                                MaterialTheme.typography.bodyMedium
                            else
                                MaterialTheme.typography.titleMedium,
                            fontWeight = if (dolzhnik.title.isBlank()) FontWeight.Bold else FontWeight.Normal,
                            color = if (dolzhnik.title.isNotBlank())
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface
                        )

                        if (dolzhnik.deadline.isNotBlank()) {
                            // показывает срок возврата долга
                            Text(
                                text = "До ${dolzhnik.deadline}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // показывает сумму долга
                Text(
                    text = "${dolzhnik.amount} ₽",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onPay,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Оплатить")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}