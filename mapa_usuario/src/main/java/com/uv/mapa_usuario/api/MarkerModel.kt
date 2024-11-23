package com.uv.mapa_usuario.api

data class MarkerModel(
    val idPrestador: Int,
    val nickname: String,
    val foto: String,
    val servicio: Int,
    val lat: Double,
    val lon: Double,
    val abre: String,
    val cierra: String,
    val calificacion: Double
)

