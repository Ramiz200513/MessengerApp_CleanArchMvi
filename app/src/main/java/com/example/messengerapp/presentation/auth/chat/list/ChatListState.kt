package com.example.messengerapp.presentation.auth.chat.list

import com.example.domain.domain.model.Chat

data class ChatListState(
    val error:String? = null,
    val chats:List<Chat> = emptyList(),
    val isLoading:Boolean = false

)