package com.example.deli

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    // Наш эндпоинт на FastAPI
    @POST("register")
    suspend fun registerUser(@Body request: UserCreateRequest): RegisterResponse

    @POST("login")
    suspend fun loginUser(@Body request: UserLoginRequest): RegisterResponse

    @POST("friends/send")
    suspend fun sendFriendRequest(@Body request: FriendSendRequest): RegisterResponse

    @POST("friends/respond")
    suspend fun respondFriendRequest(@Body request: FriendRespondRequest): RegisterResponse

    @POST("friends/unsend")
    suspend fun unsendFriendRequest(@Body request: FriendSendRequest): RegisterResponse

    @GET("friends/incoming")
    suspend fun getIncomingRequests(@Query("user_id") userId: String): List<FriendUser>

    @GET("friends/outgoing")
    suspend fun getOutgoingRequests(@Query("user_id") userId: String): List<FriendUser>

    @GET("friends/list")
    suspend fun getFriendsList(@Query("user_id") userId: String): List<FriendUser>

    @GET("users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("current_user_id") currentUserId: String
    ): List<FriendUser>
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