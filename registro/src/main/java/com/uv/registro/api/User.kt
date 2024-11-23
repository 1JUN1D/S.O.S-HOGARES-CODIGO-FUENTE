package com.uv.registro.api

data class User(
    val id: Int?,
    val nickname: String,
    val email: String,
    val password: String?,
    val email_verified: Boolean?,
    val rol: String?
)
