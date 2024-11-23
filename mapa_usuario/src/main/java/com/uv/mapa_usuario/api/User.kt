package com.uv.mapa_usuario.api

data class User(
    val id: Int?,
    val nickname: String,
    val email: String,
    val rol: String?
)
