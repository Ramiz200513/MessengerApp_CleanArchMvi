package com.example.messengerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.messengerapp.data.local.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC")
    fun getMessagesByChatId(chatId: String): Flow<List<MessageEntity>>
    @Upsert
    suspend fun upsertMessages(messages: List<MessageEntity>)
    @Query("DELETE FROM messages")
    suspend fun clearAll()
    @Query("Delete from messages where id=:id")
    suspend fun deleteMessageById(id:String)
    @Query("Update messages Set isRead = 1 Where id =:messageId")
    suspend fun markAsRead(messageId: String)
}