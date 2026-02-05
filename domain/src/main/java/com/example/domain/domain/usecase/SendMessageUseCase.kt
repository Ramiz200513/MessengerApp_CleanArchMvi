package com.example.domain.domain.usecase

import com.example.domain.domain.model.Message
import com.example.domain.domain.repository.ChatRepository
import java.util.UUID
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, text: String, userId: String) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            text = text,
            senderId = userId,
            timestamp = System.currentTimeMillis(),
            isRead = false,
        )
        repository.sendMessage(chatId, message)
    }
}
