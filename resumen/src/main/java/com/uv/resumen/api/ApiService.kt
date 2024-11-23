package com.uv.resumen.api

import com.uv.resumen.api.Service
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("users")
    fun getUserByEmail(@Query("email") email: String): Call<List<User>> // La respuesta debe estar en forma de lista


    @GET("servicios/{idusuario}")
    fun getServicesByUser(@Path("idusuario") idusuario: Int): Call<List<Service>>

    @GET("serviciosp/{idprestador}")
    fun getServicesByPrestador(@Path("idprestador") idprestador: Int): Call<List<Service>>

    @PATCH("servicio/{idservicio}/cancelar")
    fun cancelService(@Path("idservicio") idservicio: Int): Call<Void>

    @PATCH("servicio/{idservicio}/aceptar")
    fun aceptarService(@Path("idservicio") idservicio: Int): Call<Void>

    @PATCH("servicio/{idservicio}/finalizar")
    fun finalizarService(@Path("idservicio") idservicio: Int): Call<Void>

    @PATCH("servicio/{idservicio}/rechazar")
    fun rechazarService(@Path("idservicio") idservicio: Int): Call<Void>

    @PUT("servicio/resena")
    fun actualizarResena(@Body resenaRequest: ResenaRequest): Call<Void>
}