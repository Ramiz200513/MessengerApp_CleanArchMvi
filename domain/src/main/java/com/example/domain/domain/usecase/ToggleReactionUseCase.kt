package com.example.domain.domain.usecase

import com.example.domain.domain.repository.ChatRepository
import javax.inject.Inject

class ToggleReactionUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, messageId: String, emoji: String): Result<Unit> {
        return repository.toggleReaction(chatId, messageId, emoji)
    }
}