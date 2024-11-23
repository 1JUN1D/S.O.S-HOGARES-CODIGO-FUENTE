package com.uv.registro.api

data class Prestador(
    val nickname: String,
    val email: String,
    val dia_apertura: String,
    val dia_cierre: String,
    val servicio: Int,
    val tarifa: String,
    val hoja_vida: String,
    val cedula: String
)