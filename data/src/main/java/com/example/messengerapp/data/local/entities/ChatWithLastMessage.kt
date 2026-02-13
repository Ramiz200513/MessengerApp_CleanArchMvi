package com.example.messengerapp.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithLastMessage (
    @Embedded val chat: ChatEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "chatId"
    )
    val messages: List<MessageEntity>
)