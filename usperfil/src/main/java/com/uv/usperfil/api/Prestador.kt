package com.uv.usperfil.api

data class Prestador(
    val id: Int,
    val nickname: String,
    val email: String,
    val verificado: Boolean,
    val foto: String,
)