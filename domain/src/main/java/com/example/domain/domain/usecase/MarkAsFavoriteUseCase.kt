package com.example.domain.domain.usecase

import com.example.domain.domain.repository.ChatRepository
import javax.inject.Inject

class MarkAsFavoriteUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke (chatId:String){
        repository.markAsFavorite(chatId,)

    }
}