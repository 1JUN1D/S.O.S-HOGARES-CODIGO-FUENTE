package com.uv.resumen.api

data class ResenaRequest(
    val valoracion: Double,
    val comentario_val: String,
    val idservicio: Int
)