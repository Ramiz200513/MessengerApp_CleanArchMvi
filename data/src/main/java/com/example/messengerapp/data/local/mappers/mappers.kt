package com.example.messengerapp.data.local.mappers

import com.example.domain.domain.model.Chat
import com.example.domain.domain.model.Message
import com.example.domain.domain.model.User
import com.example.messengerapp.data.local.entities.ChatEntity
import com.example.messengerapp.data.local.entities.MessageEntity
import com.example.messengerapp.data.local.entities.UserEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        username = username,
        photoUrl = photoUrl,
        online = online,
        fcmToken = fcmToken
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        photoUrl = photoUrl,
        online = online,
        fcmToken = fcmToken,
    )
}

fun ChatEntity.toDomain(): Chat {
    return Chat(
        id = id,
        lastModified = lastModified,
        participants = participantsCsv.split(",").filter { it.isNotBlank() },
        isFavorite = isFavorite
    )
}

fun Chat.toEntity(): ChatEntity {
    return ChatEntity(
        id = id,
        lastModified = lastModified,
        participantsCsv = participants.joinToString(","),
        isFavorite = isFavorite
    )
}

// --- MessageMappers.kt ---

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        text = text,
        timestamp = timestamp,
        isRead = isRead,
        chatId = "",
        senderId = senderId,
        imageUrl = imageUrl,
        videoUrl = videoUrl,
        voiceUrl = voiceUrl,
        voiceDuration = voiceDuration,
        reactionsJson = Json.encodeToString(reactions),
        replyToMessageId = replyToMessageId,
        replyToMessageText = replyToMessageText,
        voiceTranscription = voiceTranscription,
        isTranscribing = isTranscribing,
        isPinned = isPinned // НОВОЕ
    )
}

fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        text = text,
        timestamp = timestamp,
        isRead = isRead,
        senderId = senderId,
        imageUrl = imageUrl,
        videoUrl = videoUrl,
        voiceUrl = voiceUrl,
        voiceDuration = voiceDuration,
        replyToMessageId = replyToMessageId,
        replyToMessageText = replyToMessageText,
        voiceTranscription = voiceTranscription,
        isTranscribing = isTranscribing,
        isPinned = isPinned, // НОВОЕ
        reactions = try {
            reactionsJson?.let { Json.decodeFromString(it) } ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    )
}