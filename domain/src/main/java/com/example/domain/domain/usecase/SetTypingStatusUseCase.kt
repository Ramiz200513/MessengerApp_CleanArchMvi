package com.example.domain.domain.usecase

import com.example.domain.domain.repository.ChatRepository
import javax.inject.Inject

class SetTypingStatusUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(
        chatId: String, isTyping: Boolean
    ){
        repository.setTypingStatus(chatId,isTyping)
    }
}