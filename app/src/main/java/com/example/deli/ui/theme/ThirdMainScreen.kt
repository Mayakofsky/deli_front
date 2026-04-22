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

            // заголовок диалога
            title = { Text("Удаление") },

            // текст с вопросом об удалении
            text = { Text("Удалить должника \"${dolzhnikToDelete!!.name}\"?") },

            // кнопка подтверждает удаление должника
            confirmButton = {
                TextButton(onClick = {
                    onDeleteDolzhnik(dolzhnikToDelete!!)
                    dolzhnikToDelete = null
                }) {
                    // текст кнопки удаления
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },

            // кнопка закрывает диалог без удаления
            dismissButton = {
                TextButton(onClick = { dolzhnikToDelete = null }) {
                    // текст кнопки отмены
                    Text("Отмена")
                }
            }
        )
    }

    if (sobitieToDelete != null) {
        // показывает диалог подтверждения удаления события
        AlertDialog(
            onDismissRequest = { sobitieToDelete = null },

            // заголовок диалога
            title = { Text("Удаление") },

            // текст с вопросом об удалении события
            text = { Text("Удалить событие от ${sobitieToDelete!!.date}?") },

            // кнопка подтверждает удаление события
            confirmButton = {
                TextButton(onClick = {
                    onDeleteSobitie(sobitieToDelete!!)
                    sobitieToDelete = null
                }) {
                    // текст кнопки удаления
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },

            // кнопка закрывает диалог без удаления
            dismissButton = {
                TextButton(onClick = { sobitieToDelete = null }) {
                    // текст кнопки отмены
                    Text("Отмена")
                }
            }
        )
    }

    // основной вертикальный контейнер экрана
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // верхняя строка с логотипом и кнопкой профиля
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // блок с названием приложения
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
                // иконка профиля
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Профиль",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // отступ после верхней панели
        Spacer(modifier = Modifier.height(16.dp))

        // строка с быстрыми действиями
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // карточка для перехода к добавлению события
            QuickActionCard(
                title = "Событие",
                icon = Icons.Default.Receipt,
                onClick = onDobavitSobitie,
                modifier = Modifier.weight(1f),
                isPrimary = false
            )

            // карточка для перехода к добавлению должника
            QuickActionCard(
                title = "Должник",
                icon = Icons.Default.PersonAdd,
                onClick = onDobavitDolshnika,
                modifier = Modifier.weight(1f),
                isPrimary = false
            )

            // карточка для перехода к друзьям
            QuickActionCard(
                title = "Друзья",
                icon = Icons.Default.Group,
                onClick = onFriends,
                modifier = Modifier.weight(1f),
                isPrimary = false
            )
        }

        // отступ перед вкладками
        Spacer(modifier = Modifier.height(16.dp))

        // строка вкладок для переключения между событиями и должниками
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                // показывает индикатор под активной вкладкой
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                // вкладка переключает страницу пейджера
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        // отображает название вкладки
                        Text(
                            text = title,
                            fontWeight = if (pagerState.currentPage == index)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // небольшой отступ после вкладок
        Spacer(modifier = Modifier.height(8.dp))

        // контейнер с перелистыванием страниц
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    if (sobitiya.isEmpty()) {
                        // показывает заглушку если событий нет
                        EmptyPlaceholder("Пока нет событий")
                    } else {
                        // показывает список событий
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(sobitiya) { sobitie ->
                                // карточка отдельного события
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
                        // показывает заглушку если должников нет
                        EmptyPlaceholder("Пока нет должников")
                    } else {
                        // показывает список должников
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(dolzhniki) { dolzhnik ->
                                // карточка отдельного должника
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
    // карточка выполняет быстрое действие по нажатию
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
        // размещает иконку и текст по центру карточки
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // отображает иконку действия
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            )

            // отступ между иконкой и текстом
            Spacer(modifier = Modifier.height(4.dp))

            // отображает название действия
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
    // контейнер для сообщения о пустом списке
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // показывает текст-заглушку
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

    // карточка с информацией о событии
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        // размещает содержимое карточки вертикально
        Column(modifier = Modifier.padding(14.dp)) {
            // верхняя строка с датой и суммой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // блок с датой и количеством участников
                Column {
                    // отображает дату события
                    Text(
                        text = sobitie.date.ifBlank { "Без даты" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

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

            // отступ перед суммой на человека
            Spacer(modifier = Modifier.height(6.dp))

            // показывает среднюю сумму на одного участника
            Text(
                text = "На каждого: ${"%.2f".format(equalShare)} ₽",
                style = MaterialTheme.typography.bodyMedium
            )

            // отступ перед кнопками
            Spacer(modifier = Modifier.height(10.dp))

            // строка с действиями для события
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // кнопка отмечает событие как оплаченное
                FilledTonalButton(
                    onClick = onPay,
                    modifier = Modifier.weight(1f)
                ) {
                    // текст кнопки оплаты
                    Text("Оплатить")
                }

                // кнопка удаляет событие
                IconButton(onClick = onDelete) {
                    // иконка удаления
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
    // карточка с информацией о должнике
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        // размещает содержимое карточки вертикально
        Column(modifier = Modifier.padding(14.dp)) {
            // верхняя строка с данными должника и суммой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // блок с аватаром и основной информацией
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
                            // показывает фото должника
                            Image(
                                painter = rememberAsyncImagePainter(dolzhnik.photoUri),
                                contentDescription = "Фото",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // показывает заглушку если фото нет
                            Box(contentAlignment = Alignment.Center) {
                                // иконка пользователя вместо фото
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // блок с именем и дедлайном
                    Column {
                        // отображает имя должника
                        Text(
                            text = dolzhnik.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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

            // отступ перед кнопками
            Spacer(modifier = Modifier.height(10.dp))

            // строка с действиями для должника
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // кнопка отмечает долг как оплаченный
                FilledTonalButton(
                    onClick = onPay,
                    modifier = Modifier.weight(1f)
                ) {
                    // текст кнопки оплаты
                    Text("Оплатить")
                }

                // кнопка удаляет должника
                IconButton(onClick = onDelete) {
                    // иконка удаления
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