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
    val isOpponentTyping: Boolean = false,
    val fullscreenImageUrl: String? = null,
    val isFavorite: Boolean = false,
    val isBiometricAuthenticated: Boolean = false,
    val replyToMessage: Message? = null,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val chatSummary: String? = null,
    val translatedMessages: Map<String, String> = emptyMap(),
    val isSummaryLoading: Boolean = false,
    val translatedImageTexts: Map<String, String> = emptyMap(),
    val loadingImageTranslations: Set<String> = emptySet(),
    val pinnedMessage: Message? = null // НОВОЕ
)