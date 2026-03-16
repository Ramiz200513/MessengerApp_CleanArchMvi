package com.example.domain.domain.usecase

import com.example.domain.domain.model.ChatWithPartner
import com.example.domain.domain.repository.ChatRepository
import com.example.domain.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMyChatsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) {
    operator fun invoke(): Flow<List<ChatWithPartner>> {
        return chatRepository.getChatsWithMessages().map { chatsWithMsgs ->
            val myId = auth.currentUser?.uid ?: ""

            chatsWithMsgs.map { item ->
                val partnerId = item.chat.participants.firstOrNull { it != myId } ?: ""
                val partnerUser = userRepository.getUserById(partnerId).getOrNull()
                val lastMsg = item.messages.maxByOrNull { it.timestamp }


                val isUnread = lastMsg != null && !lastMsg.isRead && lastMsg.senderId != myId

                ChatWithPartner(
                    chat = item.chat,
                    partner = partnerUser,
                    lastMessage = when {
                        !lastMsg?.text.isNullOrBlank() -> lastMsg?.text!!
                        !lastMsg?.imageUrl.isNullOrBlank() -> "📷 Фото"
                        else -> "Нет сообщений"
                    },
                    lastMessageTime = lastMsg?.timestamp ?: item.chat.lastModified,
                    hasUnreadMessages = isUnread // <-- Передаем статус для синего кружка
                )
            }
        }
    }
}