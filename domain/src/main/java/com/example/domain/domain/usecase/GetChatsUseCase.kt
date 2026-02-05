package com.example.domain.domain.usecase

import com.example.domain.domain.model.Chat
import com.example.domain.domain.repository.AuthRepository
import com.example.domain.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatsUseCase @Inject constructor(
    private val repository: ChatRepository
){
    operator fun invoke(): Flow<List<Chat>> {
        return repository.getChats()
    }
}