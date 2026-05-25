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

        setContent {
            
            val viewModel: MainViewModel = viewModel()
            val context = LocalContext.current
            viewModel.loadDarkTheme(context)
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            val dolzhniki = remember { mutableStateListOf<Dolzhnik>() }

            val sobitiya = remember { mutableStateListOf<Sobitie>() }

            // создает контроллер навигации между экранами
            val navController = rememberNavController()


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
                                viewModel = viewModel,
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

                                onDeleteDolzhnik = { dolzhnik ->
                                    dolzhniki.remove(dolzhnik)
                                },
                                onDeleteSobitie = { sobitie ->
                                    sobitiya.remove(sobitie)
                                },
                                onPayDolzhnik = { dolzhnik ->
                                    dolzhniki.remove(dolzhnik)
                                },
                                onPaySobitie = { sobitie ->
                                    sobitiya.remove(sobitie)
                                }
                            )
                        }

                        // экран добавления нового события
                        composable("screen_4") {
                            val userId by viewModel.userId.collectAsState()
                            DobavitSobitie(
                                innerPadding = innerPadding,
                                userId = userId,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // экран добавления нового должника
                        composable("screen_5") {
                            val userId by viewModel.userId.collectAsState()
                            DobavitDolshnika(
                                innerPadding = innerPadding,
                                userId = userId,
                                onBack = { navController.popBackStack() }
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

                    }
                }
            }
        }
    }
}