package com.uv.resumen.api

data class User(
    val id: Int?,
    val nickname: String,
    val email: String,
    val rol: String?
)
