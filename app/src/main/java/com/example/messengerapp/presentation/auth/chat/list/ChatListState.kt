package com.example.messengerapp.presentation.auth.chat.list

import com.example.domain.domain.model.Chat
import com.example.domain.domain.model.ChatWithPartner

data class ChatListState(
    val error:String? = null,
    val chats:List<ChatWithPartner> = emptyList(),
    val isLoading:Boolean = false

)