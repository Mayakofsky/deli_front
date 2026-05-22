package com.example.deli

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    // Наш эндпоинт на FastAPI
    @POST("register")
    suspend fun registerUser(@Body request: UserCreateRequest): RegisterResponse
}

object RetrofitClient {
    // Твой IP сервера (слэш в конце обязателен!)
    private const val BASE_URL = "http://195.209.213.48:8000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}