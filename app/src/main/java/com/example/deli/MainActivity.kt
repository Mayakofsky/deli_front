package com.example.deli

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deli.notifications.NotificationHelper
import com.example.deli.notifications.NotificationScheduler
import com.example.deli.ui.theme.DELITheme
import com.example.deli.ui.theme.DobavitDolshnika
import com.example.deli.ui.theme.DobavitSobitie
import com.example.deli.ui.theme.Dolzhnik
import com.example.deli.ui.theme.FriendsScreen
import com.example.deli.ui.theme.Profile
import com.example.deli.ui.theme.Sobitie
import com.example.deli.ui.theme.ThirdMainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // создает канал для отправки уведомлений
        NotificationHelper.createChannel(this)

        setContent {
            // получает основной viewmodel для темы и профиля
            val viewModel: MainViewModel = viewModel()

            // получает viewmodel для работы с друзьями
            val friendsViewModel: FriendsViewModel = viewModel()

            // получает текущий контекст приложения
            val context = LocalContext.current

            // загружает сохраненную тему приложения
            viewModel.loadDarkTheme(context)

            // отслеживает текущее состояние темы
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            // хранит список должников в памяти
            val dolzhniki = remember { mutableStateListOf<Dolzhnik>() }

            // хранит список событий в памяти
            val sobitiya = remember { mutableStateListOf<Sobitie>() }

            // получает список друзей
            val friends by friendsViewModel.friends.collectAsState()

            // получает список отправленных заявок
            val sentRequests by friendsViewModel.sentRequests.collectAsState()

            // получает список входящих заявок
            val incomingRequests by friendsViewModel.incomingRequests.collectAsState()

            // создает контроллер навигации между экранами
            val navController = rememberNavController()

            // запрашивает разрешение на уведомления
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            // запускает запрос разрешения при старте приложения
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // применяет тему ко всему интерфейсу
            DELITheme(darkTheme = isDarkTheme) {

                // основной контейнер экрана
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->

                    // задает все маршруты навигации между экранами
                    NavHost(
                        navController = navController,
                        startDestination = "screen_1"
                    ) {
                        // первый экран приветствия
                        composable(
                            route = "screen_1",
                            exitTransition = { fadeOut(animationSpec = tween(800)) }
                        ) {
                            MainScreen(
                                innerPadding = innerPadding,
                                isDarkTheme = isDarkTheme,
                                onNavigate = { navController.navigate("screen_2") }
                            )
                        }

                        // второй экран входа и регистрации
                        composable(
                            route = "screen_2",
                            enterTransition = { fadeIn(animationSpec = tween(800)) }
                        ) {
                            SecondScreen(
                                innerPadding = innerPadding,
                                onThirdMainScreen = { navController.navigate("screen_3") }
                            )
                        }

                        // главный экран со списками долгов и событий
                        composable("screen_3") {
                            ThirdMainScreen(
                                innerPadding = innerPadding,
                                dolzhniki = dolzhniki,
                                sobitiya = sobitiya,
                                onDobavitSobitie = { navController.navigate("screen_4") },
                                onDobavitDolshnika = { navController.navigate("screen_5") },
                                onProfile = { navController.navigate("screen_6") },
                                onFriends = { navController.navigate("screen_7") },

                                // удаляет должника и отменяет его уведомления
                                onDeleteDolzhnik = { dolzhnik ->
                                    NotificationScheduler.cancelDeadlineNotifications(
                                        context = context,
                                        baseId = dolzhnik.hashCode()
                                    )
                                    dolzhniki.remove(dolzhnik)
                                },

                                // удаляет событие и отменяет его уведомления
                                onDeleteSobitie = { sobitie ->
                                    NotificationScheduler.cancelDeadlineNotifications(
                                        context = context,
                                        baseId = sobitie.hashCode()
                                    )
                                    sobitiya.remove(sobitie)
                                },

                                // отмечает долг как оплаченный и убирает уведомления
                                onPayDolzhnik = { dolzhnik ->
                                    NotificationScheduler.cancelDeadlineNotifications(
                                        context = context,
                                        baseId = dolzhnik.hashCode()
                                    )
                                    dolzhniki.remove(dolzhnik)
                                },

                                // отмечает событие как оплаченное и убирает уведомления
                                onPaySobitie = { sobitie ->
                                    NotificationScheduler.cancelDeadlineNotifications(
                                        context = context,
                                        baseId = sobitie.hashCode()
                                    )
                                    sobitiya.remove(sobitie)
                                }
                            )
                        }

                        // экран добавления нового события
                        composable("screen_4") {
                            DobavitSobitie(
                                innerPadding = innerPadding,
                                friends = friends,
                                onBack = { navController.popBackStack() },

                                // добавляет событие в список и создает уведомления
                                onCreateSobitie = { sobitie ->
                                    sobitiya.add(sobitie)

                                    val equalShare = if (sobitie.participants.isNotEmpty())
                                        sobitie.totalAmount / sobitie.participants.size
                                    else 0.0

                                    // считает сумму для каждого участника
                                    val participantsInfo = sobitie.participants.joinToString("\n") { p ->
                                        val extra = p.extraAmount.toDoubleOrNull() ?: 0.0
                                        val total = equalShare + extra
                                        "- ${p.name}: ${"%.2f".format(total)} руб"
                                    }

                                    // формирует текст уведомления по событию
                                    val message = buildString {
                                        if (sobitie.name.isNotBlank()) append("${sobitie.name}\n")
                                        append("Событие от ${sobitie.date}\n")
                                        append("Общая сумма: ${"%.2f".format(sobitie.totalAmount)} руб\n")
                                        append("Кто сколько должен:\n")
                                        append(participantsInfo)
                                    }

                                    // планирует уведомления о дедлайне события
                                    NotificationScheduler.scheduleDeadlineNotifications(
                                        context = context,
                                        title = "Дедлайн события",
                                        message = message,
                                        deadline = sobitie.date,
                                        baseId = sobitie.hashCode()
                                    )
                                }
                            )
                        }

                        // экран добавления нового должника
                        composable("screen_5") {
                            DobavitDolshnika(
                                innerPadding = innerPadding,
                                friends = friends,
                                onThirdMainScreen = { navController.popBackStack() },
                                onBack = { navController.popBackStack() },

                                // добавляет должника в список и создает уведомления
                                onAddDolzhnik = { dolzhnik ->
                                    dolzhniki.add(dolzhnik)

                                    // формирует текст уведомления по долгу
                                    val message = buildString {
                                        if (dolzhnik.title.isNotBlank()) append("${dolzhnik.title}\n")
                                        append("${dolzhnik.name} должен вам ${dolzhnik.amount} руб\n")
                                        append("Дедлайн: ${dolzhnik.deadline}")
                                    }

                                    // планирует уведомления о дедлайне долга
                                    NotificationScheduler.scheduleDeadlineNotifications(
                                        context = context,
                                        title = "Дедлайн долга",
                                        message = message,
                                        deadline = dolzhnik.deadline,
                                        baseId = dolzhnik.hashCode()
                                    )
                                }
                            )
                        }

                        // экран профиля пользователя
                        composable("screen_6") {
                            val userName by viewModel.userName.collectAsState()
                            val userPhotoUri by viewModel.userPhotoUri.collectAsState()

                            Profile(
                                innerPadding = innerPadding,
                                onBack = { navController.popBackStack() },
                                isDarkTheme = isDarkTheme,

                                // переключает тему и сохраняет выбор
                                onToggleTheme = {
                                    viewModel.toggleTheme()
                                    viewModel.saveDarkTheme(context, !isDarkTheme)
                                },
                                dolzhniki = dolzhniki,
                                sobitiya = sobitiya,
                                userName = userName,
                                userPhotoUri = userPhotoUri,

                                // обновляет данные профиля
                                onUpdateProfile = { name, photo ->
                                    viewModel.updateProfile(context, name, photo)
                                }
                            )
                        }

                        // экран друзей и заявок
                        composable("screen_7") {
                            FriendsScreen(
                                innerPadding = innerPadding,
                                onBack = { navController.popBackStack() },
                                friends = friends,
                                sentRequests = sentRequests,
                                incomingRequests = incomingRequests,

                                // ищет пользователей по запросу
                                onSearch = { query -> friendsViewModel.searchUsers(query) },

                                // отправляет заявку в друзья
                                onSendRequest = { user -> friendsViewModel.sendFriendRequest(user) },

                                // отменяет отправленную заявку
                                onCancelRequest = { user -> friendsViewModel.cancelSentRequest(user) },

                                // принимает входящую заявку
                                onAcceptRequest = { user -> friendsViewModel.acceptIncomingRequest(user) },

                                // отклоняет входящую заявку
                                onDeclineRequest = { user -> friendsViewModel.declineIncomingRequest(user) },

                                // удаляет пользователя из друзей
                                onRemoveFriend = { user -> friendsViewModel.removeFriend(user) }
                            )
                        }
                    }
                }
            }
        }
    }
}