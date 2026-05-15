package com.example.deli

// Это тело запроса, которое полетит на бэк
data class UserCreateRequest(
    val email: String,
    val password: String,
    val first_name: String,
    val last_name: String,
    val link: String? = null
)

data class UserLoginRequest(
    val email: String,
    val password: String
)
data class RegisterResponse(
    val status: String,
    val user_id: String,
    val message: String
)

data class FriendSendRequest(
    val user_id: String,
    val friend_id: String
)

data class FriendRespondRequest(
    val user_id: String,
    val friend_id: String,
    val action: String
)

data class FriendUser(
    val user_id: String,
    val email: String,
    val first_name: String,
    val last_name: String
)