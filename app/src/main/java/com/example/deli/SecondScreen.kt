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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SecondScreen(
    innerPadding: PaddingValues,
    onThirdMainScreen: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // режим экрана: true = регистрация, false = вход
    var isRegistration by remember { mutableStateOf(true) }

    // имя пользователя, только для регистрации
    var firstName by remember { mutableStateOf("") }

    // фамилия пользователя, только для регистрации
    var secondName by remember { mutableStateOf("") }

    // почта пользователя, используется в обоих режимах
    var email by remember { mutableStateOf("") }

    // пароль пользователя, используется в обоих режимах
    var password by remember { mutableStateOf("") }

    // повтор пароля, только для регистрации
    var confirmPassword by remember { mutableStateOf("") }

    // показывать ли сообщения об ошибках валидации
    var showError by remember { mutableStateOf(false) }

    // true если пароль и повтор пароля совпадают
    val passwordsMatch = password == confirmPassword

    // true если все поля для входа заполнены
    val isLoginValid = email.isNotBlank() && password.isNotBlank()

    // true если все поля для регистрации заполнены и пароли совпадают
    val isRegisterValid = firstName.isNotBlank() &&
            secondName.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            passwordsMatch

    // основной контейнер экрана с отступом от шапки телефона
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(innerPadding)
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // заголовок экрана, меняется в зависимости от режима
        Text(
            text = if (isRegistration) "Регистрация" else "Вход",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // отступ после заголовка
        Spacer(modifier = Modifier.height(4.dp))

        // подзаголовок с пояснением действия
        Text(
            text = if (isRegistration) "Создайте аккаунт" else "Войдите в аккаунт",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // отступ перед переключателем режимов
        Spacer(modifier = Modifier.height(16.dp))

        // переключатель между режимами входа и регистрации
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            // кнопка переключает в режим входа
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

            // кнопка переключает в режим регистрации
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

        // отступ перед полями формы
        Spacer(modifier = Modifier.height(16.dp))

        // прокручиваемая область с полями ввода
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // поля только для режима регистрации
            if (isRegistration) {

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
            }

            // поле ввода почты, используется в обоих режимах
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Почта") },
                isError = showError && email.isBlank(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            // поле ввода пароля, текст скрыт через PasswordVisualTransformation
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                isError = showError && password.isBlank(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // поле повтора пароля только для регистрации
            if (isRegistration) {

                // поле подтверждения пароля, показывает ошибку если пароли не совпадают
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Повторите пароль") },
                    isError = showError && (confirmPassword.isBlank() || !passwordsMatch),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                // сообщение что пароли не совпадают, показывается отдельно от общей ошибки
                if (showError && confirmPassword.isNotBlank() && !passwordsMatch) {
                    Text(
                        text = "Пароли не совпадают",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // сообщение об ошибке при незаполненных обязательных полях
            if (showError && (if (isRegistration) !isRegisterValid else !isLoginValid)) {
                Text(
                    text = "Заполните все обязательные поля",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // отступ перед кнопкой действия
        Spacer(modifier = Modifier.height(8.dp))

        // кнопка подтверждения с логикой валидации
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
                                    url = "",
                                    num = 100
                                )
                                Log.d("MY_SERVER", "Записали: $firstName, ID: ${response.id}")
                            } catch (e: Exception) {
                                Log.e("MY_SERVER", "Ошибка: ${e.message}")
                            }
                        }
                    } else {
                        // показывает ошибки если поля не заполнены или пароли не совпадают
                        showError = true
                    }
                } else {
                    if (isLoginValid) {
                        showError = false
                        onThirdMainScreen()
                    } else {
                        // показывает ошибки если почта или пароль не заполнены
                        showError = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            // текст кнопки меняется в зависимости от режима
            Text(
                text = if (isRegistration) "Зарегистрироваться" else "Войти",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}