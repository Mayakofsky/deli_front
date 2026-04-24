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
import androidx.compose.runtime.mutableStateListOf

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

    // основной контейнер экрана
    Column(
        modifier = Modifier
            .fillMaxSize()
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

            // отступ между кнопкой и заголовком
            Spacer(modifier = Modifier.size(8.dp))

            // заголовок экрана
            Text(
                text = "Друзья",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // отступ перед вкладками
        Spacer(modifier = Modifier.height(12.dp))

        // строка вкладок для переключения между разделами
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                // индикатор под активной вкладкой
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                // каждая вкладка переключает страницу пейджера
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        if (index == 2 && incomingRequests.isNotEmpty()) {
                            // вкладка входящих заявок со счетчиком
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // название вкладки
                                Text(
                                    text = title,
                                    fontWeight = if (pagerState.currentPage == index)
                                        FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                // отступ перед счетчиком
                                Spacer(modifier = Modifier.size(4.dp))

                                // красный кружок с количеством входящих заявок
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        // число входящих заявок
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
                            // обычная вкладка без счетчика
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

        // отступ перед содержимым вкладок
        Spacer(modifier = Modifier.height(12.dp))

        // контейнер с перелистыванием между вкладками
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                // вкладка поиска пользователей
                0 -> SearchTab(onSearch = onSearch, onSendRequest = onSendRequest)

                // вкладка списка друзей
                1 -> FriendsTab(friends = friends, onRemoveFriend = onRemoveFriend)

                // вкладка входящих заявок
                2 -> IncomingRequestsTab(
                    requests = incomingRequests,
                    onAccept = onAcceptRequest,
                    onDecline = onDeclineRequest
                )

                // вкладка отправленных заявок
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

    // вертикальный контейнер вкладки поиска
    Column(modifier = Modifier.fillMaxSize()) {

        // поле для ввода поискового запроса
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Имя, фамилия, телефон или почта") },
            leadingIcon = {
                // иконка лупы в поле поиска
                Icon(Icons.Default.Search, contentDescription = "Поиск")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // отступ после поля поиска
        Spacer(modifier = Modifier.height(12.dp))

        when {
            // просит ввести запрос если поле пустое
            query.isBlank() -> {
                EmptyMessage("Введите имя, телефон или почту")
            }

            // сообщает что никого не найдено
            results.isEmpty() -> {
                EmptyMessage("Никого не найдено")
            }

            else -> {
                // показывает список найденных пользователей
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { user ->
                        val isSent = sentToIds.contains(user.id)

                        // карточка найденного пользователя
                        UserCard(
                            user = user,
                            actionButton = {
                                if (isSent) {
                                    // кнопка показывает что заявка уже отправлена
                                    FilledTonalButton(
                                        onClick = {},
                                        enabled = false
                                    ) {
                                        // иконка галочки на кнопке
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Отправлено",
                                            modifier = Modifier.size(18.dp)
                                        )

                                        // отступ между иконкой и текстом
                                        Spacer(modifier = Modifier.size(4.dp))

                                        // текст кнопки отправленной заявки
                                        Text("Отправлено")
                                    }
                                } else {
                                    // кнопка отправляет заявку в друзья
                                    FilledTonalButton(onClick = {
                                        onSendRequest(user)
                                        sentToIds.add(user.id)
                                    }) {
                                        // иконка добавления пользователя
                                        Icon(
                                            Icons.Default.PersonAdd,
                                            contentDescription = "Добавить",
                                            modifier = Modifier.size(18.dp)
                                        )

                                        // отступ между иконкой и текстом
                                        Spacer(modifier = Modifier.size(4.dp))

                                        // текст кнопки добавления
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
        // показывает сообщение если друзей нет
        EmptyMessage("У вас пока нет друзей")
    } else {
        // показывает список друзей
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(friends) { friend ->
                // карточка друга с кнопкой удаления
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
                            // иконка крестика для удаления
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
        // показывает сообщение если входящих заявок нет
        EmptyMessage("Нет входящих заявок")
    } else {
        // показывает список входящих заявок
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(requests) { request ->
                // карточка заявки с кнопками принятия и отклонения
                UserCard(
                    user = request.user,
                    actionButton = {
                        // строка с двумя кнопками действия
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // кнопка принимает заявку в друзья
                            FilledIconButton(
                                onClick = { onAccept(request.user) }
                            ) {
                                // иконка галочки для принятия
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Принять"
                                )
                            }

                            // кнопка отклоняет заявку
                            FilledTonalIconButton(
                                onClick = { onDecline(request.user) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                // иконка крестика для отклонения
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
        // показывает сообщение если отправленных заявок нет
        EmptyMessage("Нет отправленных заявок")
    } else {
        // показывает список отправленных заявок
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(requests) { request ->
                // карточка отправленной заявки
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    // вертикально размещает содержимое карточки
                    Column(modifier = Modifier.padding(14.dp)) {
                        // блок с данными пользователя
                        UserCardContent(user = request.user)

                        // отступ перед статусом заявки
                        Spacer(modifier = Modifier.height(8.dp))

                        // показывает статус ожидания ответа
                        Text(
                            text = "⏳ Ожидает ответа",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // отступ перед кнопкой отмены
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
        // строка с данными пользователя и кнопкой
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // блок с фото и информацией о пользователе
            UserCardContent(user = user, modifier = Modifier.weight(1f))

            // кнопка действия передается снаружи
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
                // показывает фото пользователя
                Image(
                    painter = rememberAsyncImagePainter(user.photoUri),
                    contentDescription = "Фото",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // показывает иконку если фото нет
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

        // блок с именем, телефоном и почтой
        Column(modifier = Modifier.weight(1f)) {
            // показывает полное имя пользователя
            Text(
                text = "${user.firstName} ${user.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // показывает номер телефона
            Text(
                text = user.phone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    // контейнер для сообщения о пустом разделе
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // показывает текст-заглушку по центру экрана
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}