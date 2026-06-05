package com.example.deli.repository

import com.example.deli.model.RegisterResponse
import com.example.deli.model.UserCreateRequest
import com.example.deli.model.UserLoginRequest
import com.example.deli.network.RetrofitClient

class AuthRepository {
    private val api = RetrofitClient.apiService

    suspend fun register(email: String, password: String, firstName: String, lastName: String): RegisterResponse {
        return api.registerUser(UserCreateRequest(email, password, firstName, lastName))
    }

    suspend fun login(email: String, password: String): RegisterResponse {
        return api.loginUser(UserLoginRequest(email, password))
    }
}
