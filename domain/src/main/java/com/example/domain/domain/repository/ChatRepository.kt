package com.example.domain.domain.repository

import android.net.Uri
import com.example.domain.domain.model.Chat
import com.example.domain.domain.model.ChatWithMessages
import com.example.domain.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(chatId: String, message: Message)
    fun getChats():Flow<List<Chat>>
    fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun createChat(otherUserId:String): Result<String>
    suspend fun setTypingStatus(chatId: String,isTyping: Boolean)
    fun observeChat(chatId: String): Flow<Chat>
    fun getChatsWithMessages(): Flow<List<ChatWithMessages>>
    suspend fun sendImageMessage(chatId:String,image: Uri):Result<Unit>
    suspend fun deleteMessage(chatId: String,messageId: String):Result<Unit>
    suspend fun markMessageAsRead(chatId:String,messageId: String)
}