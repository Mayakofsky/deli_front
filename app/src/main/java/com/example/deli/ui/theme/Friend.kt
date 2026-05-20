package com.example.deli.ui.theme

// Статус заявки
enum class FriendRequestStatus {
    PENDING,    // ожидает ответа
    ACCEPTED,   // принята (друг)
    SENT        // отправлена другому пользователю
}

// Пользователь для поиска
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val photoUri: String? = null
)

// Запись о друге/заявке
data class FriendRecord(
    val user: User,
    val status: FriendRequestStatus
)