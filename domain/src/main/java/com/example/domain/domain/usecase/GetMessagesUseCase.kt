package com.example.domain.domain.usecase

import com.example.domain.domain.model.Message
import com.example.domain.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
     operator fun invoke(chatId: String): Flow<List<Message>> {
        return repository.getMessages(chatId)
    }
}