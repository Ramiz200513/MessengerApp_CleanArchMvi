package com.example.domain.domain.usecase

import android.net.Uri
import com.example.domain.domain.repository.ChatRepository
import javax.inject.Inject

class SendImageMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, uri: Uri): Result<Unit> {
        return repository.sendImageMessage(chatId, uri)
    }
}