package com.example.deli

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
fun SecondScreen(innerPadding: PaddingValues, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    // Переменная для хранения введенного имени
    var firstName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Введите данные для БД")

        Spacer(modifier = Modifier.height(16.dp))

        // Поле ввода
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Имя") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.addUser(
                            fName = firstName, // ТЕПЕРЬ БЕРЕМ ИЗ ПОЛЯ ВВОДА
                            lName = "Testov",
                            url = "example.com",
                            num = 100
                        )
                        Log.d("MY_SERVER", "Записали: $firstName, ID: ${response.id}")
                    } catch (e: Exception) {
                        Log.e("MY_SERVER", "Ошибка: ${e.message}")
                    }
                }
            }
        ) {
            Text("Отправить в базу")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onBack() }) {
            Text("Назад")
        }
    }
}