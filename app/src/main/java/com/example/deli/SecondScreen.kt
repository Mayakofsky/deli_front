package com.example.deli

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SecondScreen(
    innerPadding: PaddingValues,
    onThirdMainScreen: () -> Unit
) {
    // CoroutineScope для сетевых запросов
    val scope = rememberCoroutineScope()

    // Режим экрана: вход или регистрация
    var isRegistration by remember { mutableStateOf(true) }

    // Поля формы
    var firstName by remember { mutableStateOf("") }
    var secondName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }

    // Состояние ошибок валидации
    var showError by remember { mutableStateOf(false) }

    // Проверка формы входа
    val isLoginValid = firstName.isNotBlank() && secondName.isNotBlank() && phone.isNotBlank()

    // Проверка формы регистрации
    val isRegisterValid = isLoginValid && email.isNotBlank()

    // Корневой контейнер экрана
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .navigationBarsPadding()
            .imePadding()
            .padding(24.dp)
    ) {
        // Верхняя часть экрана с формой
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Заголовок текущего режима
            Text(
                text = if (isRegistration) "Регистрация" else "Вход",
                style = MaterialTheme.typography.headlineMedium
            )

            // Отступ после заголовка
            Spacer(modifier = Modifier.height(24.dp))

            // Кнопки выбора режима
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isRegistration) {
                    // Кнопка переключения на вход
                    FilledTonalButton(
                        onClick = {
                            isRegistration = false
                            showError = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Вход")
                    }

                    // Активная кнопка регистрации
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Регистрация")
                    }
                } else {
                    // Активная кнопка входа
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Вход")
                    }

                    // Кнопка переключения на регистрацию
                    FilledTonalButton(
                        onClick = {
                            isRegistration = true
                            showError = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Регистрация")
                    }
                }
            }

            // Отступ перед формой
            Spacer(modifier = Modifier.height(24.dp))

            // Поле имени
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Имя *") },
                isError = showError && firstName.isBlank(),
                supportingText = {
                    if (showError && firstName.isBlank()) {
                        Text(
                            text = "Обязательное поле",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Отступ между полями
            Spacer(modifier = Modifier.height(8.dp))

            // Поле фамилии
            OutlinedTextField(
                value = secondName,
                onValueChange = { secondName = it },
                label = { Text("Фамилия *") },
                isError = showError && secondName.isBlank(),
                supportingText = {
                    if (showError && secondName.isBlank()) {
                        Text(
                            text = "Обязательное поле",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Отступ между полями
            Spacer(modifier = Modifier.height(8.dp))

            // Поле телефона
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Номер телефона *") },
                isError = showError && phone.isBlank(),
                supportingText = {
                    if (showError && phone.isBlank()) {
                        Text(
                            text = "Обязательное поле",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (isRegistration) {
                // Отступ перед полем почты
                Spacer(modifier = Modifier.height(8.dp))

                // Поле почты
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Почта *") },
                    isError = showError && email.isBlank(),
                    supportingText = {
                        if (showError && email.isBlank()) {
                            Text(
                                text = "Обязательное поле",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Отступ перед ссылкой
                Spacer(modifier = Modifier.height(8.dp))

                // Поле ссылки на перевод
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Ссылка на перевод") },
                    supportingText = {
                        Text(
                            text = "Необязательное поле",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showError) {
                // Отступ перед общим сообщением
                Spacer(modifier = Modifier.height(12.dp))

                // Текст общей ошибки формы
                Text(
                    text = "Заполните все обязательные поля",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Нижняя кнопка действия
        Button(
            onClick = {
                if (isRegistration) {
                    if (isRegisterValid) {
                        showError = false
                        onThirdMainScreen()

                        scope.launch {
                            try {
                                val response = RetrofitClient.apiService.addUser(
                                    fName = firstName,
                                    lName = secondName,
                                    url = link,
                                    num = 100
                                )
                                Log.d("MY_SERVER", "Записали: $firstName, ID: ${response.id}")
                            } catch (e: Exception) {
                                Log.e("MY_SERVER", "Ошибка: ${e.message}")
                            }
                        }
                    } else {
                        showError = true
                    }
                } else {
                    if (isLoginValid) {
                        showError = false
                        Log.d("MY_SERVER", "Вход: $firstName $secondName $phone")
                        onThirdMainScreen()
                    } else {
                        showError = true
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Text(text = if (isRegistration) "Зарегистрироваться" else "Войти")
        }
    }
}