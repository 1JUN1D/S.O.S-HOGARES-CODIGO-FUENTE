package com.uv.chat.api

data class Message(
    val id: Int? = null,
    val sender_id: Int,
    val receiver_id: Int,
    val message: String,
    val created_at: String? = null,
    val status: Int? = null
)
