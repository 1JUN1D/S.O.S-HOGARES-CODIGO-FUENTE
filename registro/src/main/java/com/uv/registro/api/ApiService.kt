package com.uv.registro.api
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("users")
    fun getUsers(): Call<List<User>>

    // Cambia esta parte para adecuarla al manejo por email
    @PUT("users/email/{email}")
    fun updateUserByEmail(@Path("email") email: String, @Body userInfo: User): Call<UserResponse>

    @Multipart
    @POST("users/upload")
    fun uploadFile(
        @Part("email") email: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<FileUploadResponse>

    // Agrega esto en ApiService.kt

    @Multipart
    @POST("prestadores/{nickname}")
    fun insertarPrestador(
        @Path("nickname") nickname: String,
        @Part("prestador") prestador: RequestBody,
        @Part tarifa: MultipartBody.Part,
        @Part hojaVida: MultipartBody.Part,
        @Part cedula: MultipartBody.Part,
    ): Call<UserResponse>


}