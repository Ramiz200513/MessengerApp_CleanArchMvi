package com.example.domain.domain.usecase

import com.example.domain.domain.repository.ChatRepository
import javax.inject.Inject

class TranscribeVoiceUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, messageId: String, audioUrl: String): Result<Unit> {
        return repository.transcribeVoiceMessage(chatId, messageId, audioUrl)
    }
}