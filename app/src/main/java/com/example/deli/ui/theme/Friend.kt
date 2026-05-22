package com.example.deli.ui.theme

// статус заявки в друзья
enum class FriendRequestStatus {
    // ожидает ответа
    PENDING,

    // принята, пользователь в друзьях
    ACCEPTED,

    // отправлена другому пользователю
    SENT
}

// модель пользователя для поиска и системы друзей
data class User(
    // уникальный идентификатор
    val id: String,

    // имя пользователя
    val firstName: String,

    // фамилия пользователя
    val lastName: String,

    // адрес электронной почты
    val email: String,

    // ссылка на фото профиля, может быть пустой
    val photoUri: String? = null
)

// модель записи о друге или заявке
data class FriendRecord(
    // данные пользователя
    val user: User,

    // статус отношений
    val status: FriendRequestStatus
)