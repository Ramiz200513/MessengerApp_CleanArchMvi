package com.example.domain.domain.model

import kotlinx.serialization.Serializable
@Serializable
data class Message(
    val id: String = "",
    val text: String? = null,
    val imageUrl: String? = null,
    val senderId: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)