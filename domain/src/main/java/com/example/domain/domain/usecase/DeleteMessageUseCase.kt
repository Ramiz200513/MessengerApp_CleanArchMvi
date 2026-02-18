package com.example.domain.domain.usecase

import com.example.domain.domain.repository.ChatRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, messageId: String) =
        repository.deleteMessage(chatId, messageId)
}//