package com.example.deli

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Query

// 1. Описываем, как выглядит запрос
interface ApiService {
    @POST("/add_user/")
    suspend fun addUser(
        @Query("f_name") fName: String,
        @Query("l_name") lName: String,
        @Query("url") url: String,
        @Query("num") num: Int
    ): UserResponse
}

// 2. Описываем, что сервер вернет в ответ
data class UserResponse(val status: String, val id: Int)

// 3. Создаем объект для работы с сетью
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/" // Магический IP для эмулятора

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}