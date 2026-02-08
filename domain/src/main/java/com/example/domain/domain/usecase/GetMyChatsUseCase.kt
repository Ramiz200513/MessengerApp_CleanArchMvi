package com.example.domain.domain.usecase

import com.example.domain.domain.model.ChatWithPartner
import com.example.domain.domain.repository.ChatRepository
import com.example.domain.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMyChatsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {

    operator fun invoke(): Flow<List<ChatWithPartner>> {
        return chatRepository.getChats()
            .map { chats ->


                val myIdResult = getCurrentUserUseCase()
                val myId = myIdResult.getOrNull()?.id ?: ""

                chats.map { chat ->
                    val partnerId = chat.participants.firstOrNull { it != myId } ?: ""
                    val partnerResult = userRepository.getUserById(partnerId)
                    val partnerUser = partnerResult.getOrNull()
                    val name = partnerUser?.username ?: "Неизвестный"

                    ChatWithPartner(
                        chat = chat,
                        partnerName = name,
                        partnerId = partnerId
                    )
                }
            }
    }
}