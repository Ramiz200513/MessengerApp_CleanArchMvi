package com.example.domain.domain.usecase

import com.example.domain.domain.model.Message
import com.example.domain.domain.repository.ChatRepository
import java.util.UUID
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        chatId: String,
        text: String,
        userId: String,
        replyToMessageId: String? = null,   // ДОБАВЛЯЕМ
        replyToMessageText: String? = null  // ДОБАВЛЯЕМ
    ) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            text = text,
            senderId = userId,
            replyToMessageId = replyToMessageId,     // ПЕРЕДАЕМ В МОДЕЛЬ
            replyToMessageText = replyToMessageText  // ПЕРЕДАЕМ В МОДЕЛЬ
        )
        chatRepository.sendMessage(chatId, message)
    }
}