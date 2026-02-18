package com.example.messengerapp.presentation.auth.chat.detail

import com.example.domain.domain.model.Message

data class ChatDetailState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val error: String? = null,
    val messageText: String = "",
    val currentUserId: String = "",
    val opponentName: String = "",
    val opponentImage: String? = null,
    val isOpponentTyping: Boolean = false
)