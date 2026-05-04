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