package com.example.domain.domain.model

data class ChatWithPartner(
    val chat: Chat,
    val partner: User?,
    val lastMessage: String = "Нет сообщений",
    val lastMessageTime: Long = 0L
)