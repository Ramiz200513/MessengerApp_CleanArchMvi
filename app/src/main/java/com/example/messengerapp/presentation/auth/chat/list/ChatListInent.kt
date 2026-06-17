package com.example.messengerapp.presentation.auth.chat.list

sealed class ChatListIntent {
    data class ToggleFavorite(val chatId: String) : ChatListIntent()
}