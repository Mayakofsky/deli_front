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

        NotificationHelper.createChannel(this)

        setContent {
            val viewModel: MainViewModel = viewModel()
            val friendsViewModel: FriendsViewModel = viewModel()
            val context = LocalContext.current

            viewModel.loadDarkTheme(context)

            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val dolzhniki = remember { mutableStateListOf<Dolzhnik>() }
            val sobitiya = remember { mutableStateListOf<Sobitie>() }

            val friends by friendsViewModel.friends.collectAsState()
            val sentRequests by friendsViewModel.sentRequests.collectAsState()
            val incomingRequests by friendsViewModel.incomingRequests.collectAsState()

            val navController = rememberNavController()

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            DELITheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "screen_1"
                    ) {
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

                        composable(
                            route = "screen_2",
                            enterTransition = { fadeIn(animationSpec = tween(800)) }
                        ) {
                            SecondScreen(
                                innerPadding = innerPadding,
                                onThirdMainScreen = { navController.navigate("screen_3") }
                            )
                        }

                        composable("screen_3") {
                            ThirdMainScreen(
                                innerPadding = innerPadding,
                                dolzhniki = dolzhniki,
                                sobitiya = sobitiya,
                                onDobavitSobitie = { navController.navigate("screen_4") },
                                onDobavitDolshnika = { navController.navigate("screen_5") },
                                onProfile = { navController.navigate("screen_6") },
                                onFriends = { navController.navigate("screen_7") },
                                onDeleteDolzhnik = { dolzhnik ->
                                    NotificationScheduler.cancelDeadlineNotifications(
                                        context = context,
                                        baseId = dolzhnik.hashCode()
                                    )
                                    dolzhniki.remove(dolzhnik)
                                },
                                onDeleteSobitie = { sobitie ->
                                    NotificationScheduler.cancelDeadlineNotifications(
                                        context = context,
                                        baseId = sobitie.hashCode()
                                    )
                                    sobitiya.remove(sobitie)
                                },
                                onPayDolzhnik = { dolzhnik ->
                                    NotificationScheduler.cancelDeadlineNotifications(
                                        context = context,
                                        baseId = dolzhnik.hashCode()
                                    )
                                    dolzhniki.remove(dolzhnik)
                                },
                                onPaySobitie = { sobitie ->
                                    NotificationScheduler.cancelDeadlineNotifications(
                                        context = context,
                                        baseId = sobitie.hashCode()
                                    )
                                    sobitiya.remove(sobitie)
                                }
                            )
                        }

                        composable("screen_4") {
                            DobavitSobitie(
                                innerPadding = innerPadding,
                                friends = friends,   // ← добавь
                                onBack = { navController.popBackStack() },
                                onCreateSobitie = { sobitie ->
                                    sobitiya.add(sobitie)

                                    val equalShare = if (sobitie.participants.isNotEmpty())
                                        sobitie.totalAmount / sobitie.participants.size
                                    else 0.0

                                    val participantsInfo = sobitie.participants.joinToString("\n") { p ->
                                        val extra = p.extraAmount.toDoubleOrNull() ?: 0.0
                                        val total = equalShare + extra
                                        "• ${p.name}: ${"%.2f".format(total)} ₽"
                                    }

                                    val message = """
                                        Событие от ${sobitie.date}
                                        Общая сумма: ${"%.2f".format(sobitie.totalAmount)} ₽
                                        
                                        Кто сколько должен:
                                        $participantsInfo
                                    """.trimIndent()

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

                        composable("screen_5") {
                            DobavitDolshnika(
                                innerPadding = innerPadding,
                                friends = friends,   // ← добавь
                                onThirdMainScreen = { navController.popBackStack() },
                                onBack = { navController.popBackStack() },
                                onAddDolzhnik = { dolzhnik ->
                                    dolzhniki.add(dolzhnik)

                                    val message = """
                                        ${dolzhnik.name} должен вам ${dolzhnik.amount} ₽
                                        Дедлайн: ${dolzhnik.deadline}
                                    """.trimIndent()

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

                        composable("screen_6") {
                            val userName by viewModel.userName.collectAsState()
                            val userPhotoUri by viewModel.userPhotoUri.collectAsState()

                            Profile(
                                innerPadding = innerPadding,
                                onBack = { navController.popBackStack() },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = {
                                    viewModel.toggleTheme()
                                    viewModel.saveDarkTheme(context, !isDarkTheme)
                                },
                                dolzhniki = dolzhniki,
                                sobitiya = sobitiya,
                                userName = userName,
                                userPhotoUri = userPhotoUri,
                                onUpdateProfile = { name, photo ->
                                    viewModel.updateProfile(context, name, photo)
                                }
                            )
                        }

                        composable("screen_7") {
                            FriendsScreen(
                                innerPadding = innerPadding,
                                onBack = { navController.popBackStack() },
                                friends = friends,
                                sentRequests = sentRequests,
                                incomingRequests = incomingRequests,
                                onSearch = { query -> friendsViewModel.searchUsers(query) },
                                onSendRequest = { user -> friendsViewModel.sendFriendRequest(user) },
                                onCancelRequest = { user -> friendsViewModel.cancelSentRequest(user) },
                                onAcceptRequest = { user -> friendsViewModel.acceptIncomingRequest(user) },
                                onDeclineRequest = { user -> friendsViewModel.declineIncomingRequest(user) },
                                onRemoveFriend = { user -> friendsViewModel.removeFriend(user) }
                            )
                        }
                    }
                }
            }
        }
    }
}