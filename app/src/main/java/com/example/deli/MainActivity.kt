package com.example.deli

import android.os.Build
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
import com.example.deli.ui.screens.AddDebtScreen
import com.example.deli.ui.screens.AddPurchaseScreen
import com.example.deli.ui.screens.AuthScreen
import com.example.deli.ui.screens.CreateEventScreen
import com.example.deli.ui.screens.DebtDetailScreen
import com.example.deli.ui.screens.EventDetailScreen
import com.example.deli.ui.screens.FriendsScreen
import com.example.deli.ui.screens.HomeScreen
import com.example.deli.ui.screens.ProfileScreen
import com.example.deli.ui.screens.SplashScreen
import com.example.deli.ui.theme.DELITheme
import com.example.deli.util.NotificationHelper
import com.example.deli.util.NotificationWorker
import com.example.deli.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Инициализация кэширования и канала уведомлений
        CacheHelper.init(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHelper.createNotificationChannels(this)
        }

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
                }
            }

            DELITheme(darkTheme = isDarkTheme, dynamicColor = false) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable(
                            route = "splash",
                            exitTransition = { fadeOut(animationSpec = tween(800)) }
                        ) {
                            SplashScreen(
                                innerPadding = innerPadding,
                                onNavigate = { navController.navigate("auth") }
                            )
                        }

                        composable(
                            route = "auth",
                            enterTransition = { fadeIn(animationSpec = tween(800)) }
                        ) {
                            AuthScreen(
                                innerPadding = innerPadding,
                                mainViewModel = viewModel,
                                onNavigateToHome = { navController.navigate("home") }
                            )
                        }

                        composable("home") {
                            val userId by viewModel.userId.collectAsState()
                            val userPhotoUri by viewModel.userPhotoUri.collectAsState()
                            HomeScreen(
                                innerPadding = innerPadding,
                                userId = userId,
                                userPhotoUri = userPhotoUri,
                                refreshKey = refreshTrigger.value,
                                onCreateEvent = { navController.navigate("create_event") },
                                onAddDebt = { navController.navigate("add_debt") },
                                onProfile = { navController.navigate("profile") },
                                onFriends = { navController.navigate("friends") },
                                onEventClick = { eventId -> navController.navigate("event_detail/$eventId") },
                                onDebtClick = { item, isDebtor ->
                                    viewModel.setSelectedDebtItem(item, isDebtor)
                                    navController.navigate("debt_detail")
                                }
                            )
                        }

                        composable("create_event") {
                            val userId by viewModel.userId.collectAsState()
                            CreateEventScreen(
                                innerPadding = innerPadding,
                                userId = userId,
                                onBack = { navController.popBackStack() },
                                onCreated = { eventId ->
                                    refreshTrigger.value++
                                    navController.navigate("event_detail/$eventId") {
                                        popUpTo("home")
                                    }
                                }
                            )
                        }

                        composable("add_debt") {
                            val userId by viewModel.userId.collectAsState()
                            AddDebtScreen(
                                innerPadding = innerPadding,
                                userId = userId,
                                onBack = { navController.popBackStack() },
                                onCreated = {
                                    refreshTrigger.value++
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("profile") {
                            val userPhotoUri by viewModel.userPhotoUri.collectAsState()
                            val uid by viewModel.userId.collectAsState()

                            ProfileScreen(
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
                                userPhotoUri = userPhotoUri,
                                onPhotoChanged = { url ->
                                    viewModel.updateProfilePhoto(url)
                                },
                                onLogout = {
                                    viewModel.setUserId("")
                                    viewModel.updateProfilePhoto(null)
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("friends") {
                            val userId by viewModel.userId.collectAsState()
                            FriendsScreen(
                                innerPadding = innerPadding,
                                userId = userId,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "event_detail/{eventId}",
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
                                onAddPurchase = { eId -> navController.navigate("add_purchase/$eId") }
                            )
                        }

                        composable(
                            route = "add_purchase/{eventId}",
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

                        composable("debt_detail") {
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
