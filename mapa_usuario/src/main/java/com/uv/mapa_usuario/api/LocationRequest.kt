package com.uv.mapa_usuario.api

data class LocationRequest(
    val email: String,
    val lat: Double,
    val lon: Double
)
