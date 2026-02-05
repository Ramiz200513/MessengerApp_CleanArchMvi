package com.example.messengerapp.presentation.auth.chat.detail

sealed class ChatDetailIntent {
    data class OnMessageTextChanged(val text:String): ChatDetailIntent()
    object OnSendClick: ChatDetailIntent()
    object LoadMessages: ChatDetailIntent()
}