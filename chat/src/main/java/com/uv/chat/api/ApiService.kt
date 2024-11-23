package com.uv.chat.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("users")
    fun getUserByEmail(@Query("email") email: String): Call<List<User>>

    @GET("chats/{userId}")
    suspend fun getChats(@Path("userId") userId: Int): List<Chat>

    @GET("chats/receiver/{receiverId}")
    suspend fun getChatsPrestador(@Path("receiverId") receiverId: Int): List<Chat2>

    @POST("messages")
    suspend fun addMessage(@Body message: Message): Message

    @GET("messages/{user1}/{user2}")
    suspend fun getMessages(@Path("user1") user1: Int, @Path("user2") user2: Int): List<Message>
}
