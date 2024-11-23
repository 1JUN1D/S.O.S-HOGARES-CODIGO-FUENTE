package com.uv.mapa_usuario.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://44.215.59.153:3000/"
    private const val GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }

    val googleMapsApiInstance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(GOOGLE_MAPS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}