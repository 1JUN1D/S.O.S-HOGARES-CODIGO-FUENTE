package com.uv.mapa_usuario.api

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
    fun getUserByEmail(@Query("email") email: String): Call<List<User>> // La respuesta debe estar en forma de lista

    @GET("markers")
    fun getMarkers(): Call<List<MarkerModel>>

    @GET("serviciosp/{idprestador}")
    fun getServicesByPrestador(@Path("idprestador") idprestador: Int): Call<List<Servicio>>

    @GET("comentarios/{idprestador}")
    fun getComentarios(@Path("idprestador") idprestador: Int): Call<List<Valoracion>>

    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,

    ): Call<DirectionsResponse>

    @GET("maps/api/directions/json")
    fun getDirections2(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,
        @Query("language") language: String

    ): Call<DirectionsResponse>

    @Multipart
    @POST("servicio")
    fun subirFotoServicio(
        @Part("servicio") servicio: RequestBody, // Datos adicionales como JSON
        @Part foto: MultipartBody.Part // Archivo con nombre 'foto'
    ): Call<MyApiResponse> // Ajusta la clase de respuesta según tu estructura

    @PUT("prestador/ubicacion")
    fun updateLocation(@Body locationRequest: LocationRequest): Call<SimpleResponse>
}
