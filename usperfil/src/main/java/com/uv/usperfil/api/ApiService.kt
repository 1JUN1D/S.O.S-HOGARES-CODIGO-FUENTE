package com.uv.usperfil.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("users")
    fun getUserByEmail(@Query("email") email: String): Call<List<User>> // La respuesta debe estar en forma de lista

    @GET("/prestador")
    fun getPrestadorByEmail(@Query("email") email: String): Call<Prestador>

    @Multipart
    @POST("actualizarFotoUsuario")
    fun actualizarFotoUsuario(
        @Part user: MultipartBody.Part,
        @Part foto: MultipartBody.Part
    ): Call<Void>

    @Multipart
    @POST("actualizarFotoPrestador")
    fun actualizarFotoPrestador(
        @Part prestador: MultipartBody.Part,
        @Part foto: MultipartBody.Part
    ): Call<Void>

    @PUT("/updatenickname")
    fun updateNicknameByEmail(
        @Query("email") email: String,
        @Query("nickname") nickname: String
    ): Call<Void>
}