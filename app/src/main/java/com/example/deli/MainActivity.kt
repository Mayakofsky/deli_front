package com.example.deli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.deli.data.CacheHelper
import com.example.deli.ui.theme.AddPurchaseScreen
import com.example.deli.ui.theme.DebtDetailScreen
import com.example.deli.ui.theme.DELITheme
import com.example.deli.ui.theme.DobavitDolshnika
import com.example.deli.ui.theme.DobavitSobitie
import com.example.deli.ui.theme.EventDetailScreen
import com.example.deli.ui.theme.FriendsScreen
import com.example.deli.ui.theme.Profile
import com.example.deli.ui.theme.ThirdMainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        CacheHelper.init(this)
        NotificationHelper.createNotificationChannels(this)

        setContent {
            val viewModel: MainViewModel = viewModel()
            val context = LocalContext.current
            viewModel.loadDarkTheme(context)
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val userId by viewModel.userId.collectAsState()
            val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
            val navController = rememberNavController()
            val refreshTrigger = remember { mutableStateOf(0) }

            LaunchedEffect(userId, notificationsEnabled) {
                if (userId.isNotEmpty() && notificationsEnabled) {
                    NotificationWorker.schedule(context, userId)
                } else {
                    NotificationWorker.cancel(context)
                    if (userId.isEmpty()) NotificationPrefs.clearAll(context)
                }
            }

            DELITheme(darkTheme = isDarkTheme, dynamicColor = false) {
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
                                mainViewModel = viewModel,
                                onThirdMainScreen = { navController.navigate("screen_3") }
                            )
                        }

                        composable("screen_3") {
                            val userId by viewModel.userId.collectAsState()
                            ThirdMainScreen(
                                innerPadding = innerPadding,
                                userId = userId,
                                refreshKey = refreshTrigger.value,
                                onDobavitSobitie = { navController.navigate("screen_4") },
                                onDobavitDolshnika = { navController.navigate("screen_5") },
                                onProfile = { navController.navigate("screen_6") },
                                onFriends = { navController.navigate("screen_7") },
                                onEventClick = { eventId -> navController.navigate("screen_8/$eventId") },
                                onDebtClick = { item ->
                                    viewModel.setSelectedDebtItem(item)
                                    navController.navigate("screen_10")
                                }
                            )
                        }

                        composable("screen_4") {
                            val userId by viewModel.userId.collectAsState()
                            DobavitSobitie(
                                innerPadding = innerPadding,
                                userId = userId,
                                onBack = { navController.popBackStack() },
                                onCreated = { eventId ->
                                    refreshTrigger.value++
                                    navController.navigate("screen_8/$eventId")
                                }
                            )
                        }

                        composable("screen_5") {
                            val userId by viewModel.userId.collectAsState()
                            DobavitDolshnika(
                                innerPadding = innerPadding,
                                userId = userId,
                                onBack = { navController.popBackStack() },
                                onCreated = {
                                    refreshTrigger.value++
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("screen_6") {
                            val userName by viewModel.userName.collectAsState()
                            val userPhotoUri by viewModel.userPhotoUri.collectAsState()
                            val uid by viewModel.userId.collectAsState()

                            Profile(
                                innerPadding = innerPadding,
                                onBack = { navController.popBackStack() },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = {
                                    viewModel.toggleTheme()
                                    viewModel.saveDarkTheme(context, !isDarkTheme)
                                },
                                notificationsEnabled = notificationsEnabled,
                                onToggleNotifications = {
                                    viewModel.toggleNotifications()
                                    viewModel.saveNotificationsEnabled(context, !notificationsEnabled)
                                },
                                userId = uid,
                                userName = userName,
                                userPhotoUri = userPhotoUri,
                                onUpdateProfile = { name, photo ->
                                    viewModel.updateProfile(context, name, photo)
                                },
                                onLogout = {
                                    viewModel.setUserId("")
                                    viewModel.updateProfile(context, "Пользователь", null)
                                    navController.navigate("screen_2") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("screen_7") {
                            val userId by viewModel.userId.collectAsState()
                            FriendsScreen(
                                innerPadding = innerPadding,
                                userId = userId,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "screen_8/{eventId}",
                            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId by viewModel.userId.collectAsState()
                            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                            EventDetailScreen(
                                innerPadding = innerPadding,
                                eventId = eventId,
                                userId = userId,
                                refreshKey = refreshTrigger.value,
                                onBack = { navController.popBackStack() },
                                onAddPurchase = { eId -> navController.navigate("screen_9/$eId") }
                            )
                        }

                        composable(
                            route = "screen_9/{eventId}",
                            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId by viewModel.userId.collectAsState()
                            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                            AddPurchaseScreen(
                                innerPadding = innerPadding,
                                eventId = eventId,
                                userId = userId,
                                onBack = { navController.popBackStack() },
                                onCreated = {
                                    refreshTrigger.value++
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("screen_10") {
                            DebtDetailScreen(
                                innerPadding = innerPadding,
                                mainViewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onDeleted = { refreshTrigger.value++ }
                            )
                        }
                    }
                }
            }
        }
    }
}
