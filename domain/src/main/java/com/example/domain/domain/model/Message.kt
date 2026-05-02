package com.example.domain.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String = "",
    val text: String? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val voiceUrl: String? = null,
    val voiceDuration: Int? = null,
    val reactions: Map<String, List<String>> = emptyMap(),
    val senderId: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
    val replyToMessageId: String? = null,
    val replyToMessageText: String? = null,
    val voiceTranscription: String? = null,
    val isTranscribing: Boolean = false,
    val isPinned: Boolean = false // НОВОЕ
)