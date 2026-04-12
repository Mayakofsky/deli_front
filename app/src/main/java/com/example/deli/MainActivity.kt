package com.example.deli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deli.ui.theme.DELITheme
import com.example.deli.ui.theme.DobavitDolshnika
import com.example.deli.ui.theme.DobavitSobitie
import com.example.deli.ui.theme.Profile
import com.example.deli.ui.theme.ThirdMainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Подключение splash screen
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // ViewModel приложения
            val viewModel: MainViewModel = viewModel()

            // Контекст для работы с DataStore
            val context = LocalContext.current

            // Загрузка сохраненной темы
            viewModel.loadDarkTheme(context)

            // Состояния из ViewModel
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val dolzhniki by viewModel.dolzhniki.collectAsState()
            val sobitiya by viewModel.sobitiya.collectAsState()

            // Контроллер навигации
            val navController = rememberNavController()

            // Тема приложения
            DELITheme(darkTheme = isDarkTheme) {
                // Общий контейнер экранов
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Навигация между экранами
                    NavHost(
                        navController = navController,
                        startDestination = "screen_1"
                    ) {
                        // Стартовый экран
                        composable("screen_1") {
                            MainScreen(
                                innerPadding = innerPadding,
                                onNavigate = { navController.navigate("screen_2") }
                            )
                        }

                        // Экран входа и регистрации
                        composable("screen_2") {
                            SecondScreen(
                                innerPadding = innerPadding,
                                onThirdMainScreen = { navController.navigate("screen_3") }
                            )
                        }

                        // Главный экран приложения
                        composable("screen_3") {
                            ThirdMainScreen(
                                innerPadding = innerPadding,
                                dolzhniki = dolzhniki,
                                sobitiya = sobitiya,
                                onDobavitSobitie = { navController.navigate("screen_7") },
                                onDobavitDolshnika = { navController.navigate("screen_5") },
                                onProfile = { navController.navigate("screen_6") },
                                onDeleteDolzhnik = { dolzhnik ->
                                    viewModel.removeDolzhnik(dolzhnik)
                                },
                                onDeleteSobitie = { sobitie ->
                                    viewModel.removeSobitie(sobitie)
                                }
                            )
                        }

                        // Экран добавления должника
                        composable("screen_5") {
                            DobavitDolshnika(
                                innerPadding = innerPadding,
                                onThirdMainScreen = { navController.popBackStack() },
                                onBack = { navController.popBackStack() },
                                onAddDolzhnik = { dolzhnik ->
                                    viewModel.addDolzhnik(dolzhnik)
                                }
                            )
                        }

                        // Экран профиля
                        composable("screen_6") {
                            Profile(
                                innerPadding = innerPadding,
                                onBack = { navController.popBackStack() },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = {
                                    viewModel.toggleTheme()
                                    viewModel.saveDarkTheme(context, !isDarkTheme)
                                },
                                dolzhniki = dolzhniki,
                                sobitiya = sobitiya
                            )
                        }

                        // Экран добавления события
                        composable("screen_7") {
                            DobavitSobitie(
                                innerPadding = innerPadding,
                                onBack = { navController.popBackStack() },
                                onCreateSobitie = { sobitie ->
                                    viewModel.addSobitie(sobitie)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}