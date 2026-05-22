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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendsScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    friends: List<FriendRecord>,
    sentRequests: List<FriendRecord>,
    incomingRequests: List<FriendRecord>,
    onSearch: (String) -> List<User>,
    onSendRequest: (User) -> Unit,
    onCancelRequest: (User) -> Unit,
    onAcceptRequest: (User) -> Unit,
    onDeclineRequest: (User) -> Unit,
    onRemoveFriend: (User) -> Unit
) {
    // состояние пейджера с четырьмя вкладками
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    // названия вкладок
    val tabs = listOf("Поиск", "Друзья", "Входящие", "Отправл.")

    // основной контейнер экрана с отступом от шапки
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // верхняя строка с кнопкой назад и заголовком
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // кнопка возвращает на предыдущий экран
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }

            Spacer(modifier = Modifier.size(8.dp))

            // заголовок экрана
            Text(
                text = "Друзья",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // строка вкладок для переключения между разделами
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
                        if (index == 2 && incomingRequests.isNotEmpty()) {
                            // вкладка входящих заявок со счетчиком
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    fontWeight = if (pagerState.currentPage == index)
                                        FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.size(4.dp))

                                // красный кружок с количеством входящих заявок
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = incomingRequests.size.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onError,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = title,
                                fontWeight = if (pagerState.currentPage == index)
                                    FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // контейнер с перелистыванием между вкладками
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> SearchTab(onSearch = onSearch, onSendRequest = onSendRequest)
                1 -> FriendsTab(friends = friends, onRemoveFriend = onRemoveFriend)
                2 -> IncomingRequestsTab(
                    requests = incomingRequests,
                    onAccept = onAcceptRequest,
                    onDecline = onDeclineRequest
                )
                3 -> SentRequestsTab(
                    requests = sentRequests,
                    onCancel = onCancelRequest
                )
            }
        }
    }
}

@Composable
fun SearchTab(
    onSearch: (String) -> List<User>,
    onSendRequest: (User) -> Unit
) {
    // хранит введенный поисковый запрос
    var query by remember { mutableStateOf("") }

    // хранит результаты поиска
    var results by remember { mutableStateOf<List<User>>(emptyList()) }

    // хранит id пользователей которым уже отправлена заявка
    val sentToIds = remember { mutableStateListOf<String>() }

    // обновляет результаты при каждом изменении запроса
    LaunchedEffect(query) {
        results = onSearch(query)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // поле для ввода поискового запроса
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Имя, фамилия или почта") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Поиск")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            query.isBlank() -> {
                EmptyMessage("Введите имя или почту")
            }
            results.isEmpty() -> {
                EmptyMessage("Никого не найдено")
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { user ->
                        val isSent = sentToIds.contains(user.id)

                        UserCard(
                            user = user,
                            actionButton = {
                                if (isSent) {
                                    // кнопка показывает что заявка уже отправлена
                                    FilledTonalButton(
                                        onClick = {},
                                        enabled = false
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Отправлено",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text("Отправлено")
                                    }
                                } else {
                                    // кнопка отправляет заявку в друзья
                                    FilledTonalButton(onClick = {
                                        onSendRequest(user)
                                        sentToIds.add(user.id)
                                    }) {
                                        Icon(
                                            Icons.Default.PersonAdd,
                                            contentDescription = "Добавить",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text("Добавить")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendsTab(
    friends: List<FriendRecord>,
    onRemoveFriend: (User) -> Unit
) {
    if (friends.isEmpty()) {
        EmptyMessage("У вас пока нет друзей")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(friends) { friend ->
                UserCard(
                    user = friend.user,
                    actionButton = {
                        // кнопка удаляет пользователя из друзей
                        FilledTonalIconButton(
                            onClick = { onRemoveFriend(friend.user) },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun IncomingRequestsTab(
    requests: List<FriendRecord>,
    onAccept: (User) -> Unit,
    onDecline: (User) -> Unit
) {
    if (requests.isEmpty()) {
        EmptyMessage("Нет входящих заявок")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(requests) { request ->
                UserCard(
                    user = request.user,
                    actionButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // кнопка принимает заявку в друзья
                            FilledIconButton(
                                onClick = { onAccept(request.user) }
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Принять")
                            }

                            // кнопка отклоняет заявку
                            FilledTonalIconButton(
                                onClick = { onDecline(request.user) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Отклонить",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SentRequestsTab(
    requests: List<FriendRecord>,
    onCancel: (User) -> Unit
) {
    if (requests.isEmpty()) {
        EmptyMessage("Нет отправленных заявок")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(requests) { request ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        UserCardContent(user = request.user)

                        Spacer(modifier = Modifier.height(8.dp))

                        // показывает статус ожидания ответа
                        Text(
                            text = "Ожидает ответа",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // кнопка отменяет отправленную заявку
                        FilledTonalButton(
                            onClick = { onCancel(request.user) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Отменить заявку")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    actionButton: @Composable () -> Unit
) {
    // карточка пользователя с данными и кнопкой действия
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            UserCardContent(user = user, modifier = Modifier.weight(1f))
            actionButton()
        }
    }
}

@Composable
fun UserCardContent(
    user: User,
    modifier: Modifier = Modifier
) {
    // строка с аватаром и текстовыми данными
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // контейнер для фото или иконки пользователя
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            if (user.photoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(user.photoUri),
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

        // блок с именем и почтой
        Column(modifier = Modifier.weight(1f)) {
            // показывает полное имя пользователя
            Text(
                text = "${user.firstName} ${user.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // показывает адрес электронной почты
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyMessage(text: String) {
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