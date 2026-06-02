package com.example.deli

import android.content.Context
import android.net.Uri
import com.example.deli.data.CacheHelper
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserRepository {
    private val api = RetrofitClient.apiService
    private val gson = Gson()

    suspend fun getUser(userId: String): UserResponse {
        return try {
            val data = api.getUser(userId)
            try {
                CacheHelper.saveUser(gson.toJson(data))
            } catch (_: Exception) {}
            data
        } catch (e: Exception) {
            if (CacheHelper.isInitialized()) {
                val cached = CacheHelper.getUser(userId)
                if (cached != null) return gson.fromJson(cached, UserResponse::class.java)
            }
            throw e
        }
    }

    suspend fun updateUser(userId: String, request: UserUpdateRequest) {
        api.updateUser(userId, request)
    }

    suspend fun uploadPhoto(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open file")
        val bytes = inputStream.readBytes()
        inputStream.close()
        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", "photo.jpg", requestBody)
        val response = api.uploadPhoto(part)
        return response.url
    }
}
