package com.example.domain.domain.model

data class ChatWithMessages(
    val chat: Chat,
    val messages: List<Message>
)