package com.uv.usperfil.api

import com.uv.usperfil.api.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://44.215.59.153:3000/"  // Reemplaza IP_DE_TU_SERVIDOR con la IP real de tu servidor

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }

}
