package com.example.domain.domain.usecase

import com.example.domain.domain.repository.ChatRepository
import javax.inject.Inject

class PinMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, messageId: String, pin: Boolean): Result<Unit> {
        return repository.pinMessage(chatId, messageId, pin)
    }
}