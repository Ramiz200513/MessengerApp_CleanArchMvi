package com.example.messengerapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity (
    @PrimaryKey val id: String,
    val chatId: String,
    val text: String?,
    val senderId: String,
    val timestamp: Long,
    val isRead: Boolean,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val voiceUrl: String? = null,
    val voiceDuration: Int? = null,
    val reactionsJson: String? = null
)