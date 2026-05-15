package com.example.deli.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deli.FriendSendRequest
import com.example.deli.FriendUser
import com.example.deli.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendsScreen(
    innerPadding: PaddingValues,
    userId: String,
    onBack: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Поиск", "Входящие", "Друзья", "Отправленные")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Text(
                text = "Друзья",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> SearchTab(userId = userId)
                1 -> IncomingTab(userId = userId)
                2 -> FriendsTab(userId = userId)
                3 -> SentTab(userId = userId)
            }
        }
    }
}

@Composable
private fun SearchTab(userId: String) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<FriendUser>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var sentIds by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Введите имя или почту") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    loading = true
                    try {
                        results = RetrofitClient.apiService.searchUsers(query.trim(), userId)
                    } catch (_: Exception) {
                        results = emptyList()
                    }
                    loading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Найти")
            }
        }

        if (results.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(results) { user ->
                    RequestCard(
                        name = "${user.first_name} ${user.last_name}",
                        subtitle = user.email,
                        actions = {
                            if (user.user_id in sentIds) {
                                Button(
                                    onClick = {},
                                    enabled = false
                                ) {
                                    Text("Запрос отправлен")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                RetrofitClient.apiService.sendFriendRequest(
                                                    FriendSendRequest(userId, user.user_id)
                                                )
                                                sentIds = sentIds + user.user_id
                                            } catch (_: Exception) {}
                                        }
                                    }
                                ) {
                                    Text("Добавить")
                                }
                            }
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (loading) "Поиск..." else "Введите имя для поиска",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IncomingTab(userId: String) {
    val incoming = remember { mutableStateListOf<FriendUser>() }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val items = RetrofitClient.apiService.getIncomingRequests(userId)
            incoming.clear()
            incoming.addAll(items)
        } catch (_: Exception) {}
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (incoming.isEmpty()) {
        EmptyPlaceholder("Нет входящих запросов")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(incoming.toList()) { user ->
                RequestCard(
                    name = "${user.first_name} ${user.last_name}",
                    subtitle = "хочет добавить вас в друзья",
                    actions = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.apiService.respondFriendRequest(
                                                com.example.deli.FriendRespondRequest(
                                                    userId, user.user_id, "accept"
                                                )
                                            )
                                            incoming.remove(user)
                                        } catch (_: Exception) {}
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.size(4.dp))
                                Text("Принять")
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.apiService.respondFriendRequest(
                                                com.example.deli.FriendRespondRequest(
                                                    userId, user.user_id, "reject"
                                                )
                                            )
                                            incoming.remove(user)
                                        } catch (_: Exception) {}
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.size(4.dp))
                                Text("Отклонить")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FriendsTab(userId: String) {
    val friends = remember { mutableStateListOf<FriendUser>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val items = RetrofitClient.apiService.getFriendsList(userId)
            friends.clear()
            friends.addAll(items)
        } catch (_: Exception) {}
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (friends.isEmpty()) {
        EmptyPlaceholder("Нет друзей")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(friends.toList()) { user ->
                RequestCard(
                    name = "${user.first_name} ${user.last_name}",
                    subtitle = user.email,
                    actions = {}
                )
            }
        }
    }
}

@Composable
private fun SentTab(userId: String) {
    val sent = remember { mutableStateListOf<FriendUser>() }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val items = RetrofitClient.apiService.getOutgoingRequests(userId)
            sent.clear()
            sent.addAll(items)
        } catch (_: Exception) {}
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (sent.isEmpty()) {
        EmptyPlaceholder("Нет отправленных запросов")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(sent.toList()) { user ->
                RequestCard(
                    name = "${user.first_name} ${user.last_name}",
                    subtitle = "Ожидание",
                    actions = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        RetrofitClient.apiService.unsendFriendRequest(
                                            FriendSendRequest(userId, user.user_id)
                                        )
                                        sent.remove(user)
                                    } catch (_: Exception) {}
                                }
                            }
                        ) {
                            Text("Отменить", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RequestCard(
    name: String,
    subtitle: String?,
    actions: @Composable () -> Unit
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                actions()
            }
        }
    }
}

