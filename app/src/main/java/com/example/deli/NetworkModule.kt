package com.example.deli

import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
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

    @POST("users/guest")
    suspend fun createGuest(@Body request: GuestCreateRequest): EventParticipant

    @POST("events")
    suspend fun createEvent(@Body request: EventCreateRequest): EventResponse

    @GET("events")
    suspend fun listEvents(@Query("user_id") userId: String): List<EventResponse>

    @GET("events/{eventId}")
    suspend fun getEvent(@Path("eventId") eventId: String): EventResponse

    @POST("events/{eventId}/participants")
    suspend fun addParticipant(@Path("eventId") eventId: String, @Body request: ParticipantAddRequest): OkResponse

    @DELETE("events/{eventId}/participants/{userId}")
    suspend fun removeParticipant(@Path("eventId") eventId: String, @Path("userId") userId: String): OkResponse

    @GET("events/{eventId}/participants")
    suspend fun listParticipants(@Path("eventId") eventId: String): List<EventParticipant>

    @POST("events/{eventId}/guests")
    suspend fun addGuest(@Path("eventId") eventId: String, @Body request: GuestCreateRequest): EventParticipant

    @POST("events/{eventId}/purchases")
    suspend fun addPurchase(@Path("eventId") eventId: String, @Body request: PurchaseCreateRequest): PurchaseResponse

    @GET("events/{eventId}/purchases")
    suspend fun listPurchases(@Path("eventId") eventId: String): List<PurchaseResponse>

    @PATCH("events/{eventId}/purchases/{purchaseId}")
    suspend fun updatePurchase(@Path("eventId") eventId: String, @Path("purchaseId") purchaseId: String, @Body request: PurchaseUpdateRequest): PurchaseResponse

    @GET("events/{eventId}/balances")
    suspend fun getBalances(@Path("eventId") eventId: String): List<BalanceItem>

    @GET("events/{eventId}/confirmations")
    suspend fun getEventConfirmations(@Path("eventId") eventId: String): List<String>

    @POST("events/{eventId}/confirm")
    suspend fun confirmEvent(@Path("eventId") eventId: String, @Body request: EventConfirmRequest): EventResponse

    @DELETE("events/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: String): OkResponse

    @POST("events/{eventId}/close")
    suspend fun closeEvent(@Path("eventId") eventId: String): OkResponse

    @POST("debts")
    suspend fun createDebt(@Body request: DebtCreateRequest): DebtResponse

    @GET("debts")
    suspend fun listDebts(@Query("user_id") userId: String, @Query("status") status: String? = null): List<DebtResponse>

    @GET("debts/{debtId}")
    suspend fun getDebt(@Path("debtId") debtId: String): DebtResponse

    @PATCH("debts/{debtId}")
    suspend fun updateDebt(@Path("debtId") debtId: String, @Body request: DebtUpdateRequest): OkResponse

    @DELETE("debts/{debtId}")
    suspend fun deleteDebt(@Path("debtId") debtId: String): OkResponse

    @GET("summary/owed")
    suspend fun summaryOwed(@Query("user_id") userId: String): List<SummaryItem>

    @GET("summary/due")
    suspend fun summaryDue(@Query("user_id") userId: String): List<SummaryItem>

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): UserResponse

    @PATCH("users/{userId}")
    suspend fun updateUser(@Path("userId") userId: String, @Body request: UserUpdateRequest): OkResponse

    @Multipart
    @POST("upload")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): UploadResponse
}

object RetrofitClient {
    const val BASE_URL = "http://195.209.213.48:8000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun fullUrl(path: String): String = BASE_URL + path.removePrefix("/")
}
