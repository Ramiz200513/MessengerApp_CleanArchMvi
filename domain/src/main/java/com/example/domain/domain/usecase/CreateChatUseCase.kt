package com.example.domain.domain.usecase

import com.example.domain.domain.repository.ChatRepository
import javax.inject.Inject

class CreateChatUseCase @Inject constructor(
    private val repository: ChatRepository
){
    suspend operator fun invoke(otherUserId:String):Result<String>{
        return repository.createChat(otherUserId)
    }
}