package com.example.deli

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SecondScreen(
    innerPadding: PaddingValues,
    onThirdMainScreen: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var isRegistration by remember { mutableStateOf(true) }
    var firstName by remember { mutableStateOf("") }
    var secondName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val isLoginValid = firstName.isNotBlank() && secondName.isNotBlank() && phone.isNotBlank()
    val isRegisterValid = isLoginValid && email.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // заголовок экрана, меняется в зависимости от режима (вход/регистрация)
        Text(
            text = if (isRegistration) "Регистрация" else "Вход",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // подзаголовок с пояснением действия
        Text(
            text = if (isRegistration) "Создайте аккаунт" else "Войдите в аккаунт",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // переключатель между режимами входа и регистрации
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            // кнопка режима "Вход"
            SegmentedButton(
                selected = !isRegistration,
                onClick = {
                    isRegistration = false
                    showError = false
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Вход")
            }
            // кнопка режима "Регистрация"
            SegmentedButton(
                selected = isRegistration,
                onClick = {
                    isRegistration = true
                    showError = false
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Регистрация")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // прокручиваемая область с полями ввода
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // поле ввода имени с валидацией
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Имя") },
                isError = showError && firstName.isBlank(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // поле ввода фамилии с валидацией
            OutlinedTextField(
                value = secondName,
                onValueChange = { secondName = it },
                label = { Text("Фамилия") },
                isError = showError && secondName.isBlank(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // поле ввода телефона с цифровой клавиатурой
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Номер телефона") },
                isError = showError && phone.isBlank(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            // дополнительные поля только для регистрации
            if (isRegistration) {
                // поле ввода email с соответствующей клавиатурой
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Почта") },
                    isError = showError && email.isBlank(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                // необязательное поле для ссылки на перевод
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Ссылка на перевод") },
                    supportingText = { Text("Необязательно") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // сообщение об ошибке при незаполненных полях
            if (showError) {
                Text(
                    text = "Заполните все обязательные поля",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // основная кнопка подтверждения с логикой валидации и отправки данных
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
                    } else showError = true
                } else {
                    if (isLoginValid) {
                        showError = false
                        onThirdMainScreen()
                    } else showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (isRegistration) "Зарегистрироваться" else "Войти",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
