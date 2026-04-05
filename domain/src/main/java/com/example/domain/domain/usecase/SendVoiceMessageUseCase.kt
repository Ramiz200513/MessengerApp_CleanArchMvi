package com.example.domain.domain.usecase

import android.net.Uri
import com.example.domain.domain.repository.ChatRepository
import javax.inject.Inject

class SendVoiceMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, uri: Uri, duration: Int): Result<Unit> {
        return repository.sendVoiceMessage(chatId, uri, duration)
    }
}