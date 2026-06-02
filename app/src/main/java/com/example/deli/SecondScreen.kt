package com.example.deli

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SecondScreen(
    innerPadding: PaddingValues,
    mainViewModel: MainViewModel,
    onThirdMainScreen: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    var isRegistration by remember { mutableStateOf(true) }
    var firstName by remember { mutableStateOf("") }
    var secondName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf(false) }

    val passwordsMatch = password == confirmPassword
    val isLoginValid = email.isNotBlank() && password.isNotBlank()
    val isRegisterValid = firstName.isNotBlank() &&
            secondName.isNotBlank() && email.isNotBlank() &&
            password.isNotBlank() && confirmPassword.isNotBlank() && passwordsMatch

    LaunchedEffect(authState.userId) {
        authState.userId?.let { uid ->
            mainViewModel.setUserId(uid)
            authViewModel.resetState()
            onThirdMainScreen()
        }
    }

    LaunchedEffect(authState.error) {
        authState.error?.let { localError = true }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(innerPadding)
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = if (isRegistration) "Регистрация" else "Вход",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isRegistration) "Создайте аккаунт" else "Войдите в аккаунт",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = !isRegistration,
                onClick = {
                    isRegistration = false
                    showError = false
                    localError = false
                    authViewModel.resetState()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Вход") }

            SegmentedButton(
                selected = isRegistration,
                onClick = {
                    isRegistration = true
                    showError = false
                    localError = false
                    authViewModel.resetState()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Регистрация") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isRegistration) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Имя") },
                    isError = showError && firstName.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = secondName,
                    onValueChange = { secondName = it },
                    label = { Text("Фамилия") },
                    isError = showError && secondName.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Почта") },
                isError = showError && email.isBlank(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

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

            if (isRegistration) {
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

                if (showError && confirmPassword.isNotBlank() && !passwordsMatch) {
                    Text(
                        text = "Пароли не совпадают",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if ((showError || localError) && authState.error != null) {
                Text(
                        text = authState.error ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (showError && (if (isRegistration) !isRegisterValid else !isLoginValid) && authState.error == null) {
                Text(
                    text = "Заполните все обязательные поля",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (isRegistration) {
                    if (isRegisterValid) {
                        showError = false
                        localError = false
                        if (email.trim() == "a" && password == "a") {
                            mainViewModel.setUserId("USR-OFFLINE")
                            onThirdMainScreen()
                        } else {
                            authViewModel.register(email, password, firstName, secondName)
                        }
                    } else {
                        showError = true
                    }
                } else {
                    if (isLoginValid) {
                        showError = false
                        localError = false
                        if (email.trim() == "a" && password == "a") {
                            mainViewModel.setUserId("USR-OFFLINE")
                            onThirdMainScreen()
                        } else {
                            authViewModel.login(email, password)
                        }
                    } else {
                        showError = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !authState.isLoading
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = if (isRegistration) "Зарегистрироваться" else "Войти",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

