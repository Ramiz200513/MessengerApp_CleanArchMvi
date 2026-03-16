package com.example.domain.domain.usecase

import android.net.Uri
import com.example.domain.domain.repository.ChatRepository
import javax.inject.Inject

class SendVideoMessageUseCase @Inject constructor(
    private val repo: ChatRepository
){
    suspend operator fun invoke (chatId:String,uri: Uri):Result<Unit>{
        return repo.sendVideoMessage(chatId,uri)
    }
}