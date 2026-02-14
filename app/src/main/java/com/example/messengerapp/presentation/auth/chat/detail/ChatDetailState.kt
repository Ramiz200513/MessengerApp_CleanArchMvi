package com.example.messengerapp.presentation.auth.chat.detail

import com.example.domain.domain.model.Message

data class ChatDetailState (
    val messages:List<Message> = emptyList(),
    val error:String? = null,
    val isLoading: Boolean = false,
    val messageText:String = "",
    val currentUserId: String = "",
    val isOpponentTyping: Boolean = false,
    val opponentName: String = "Загрузка..."
)