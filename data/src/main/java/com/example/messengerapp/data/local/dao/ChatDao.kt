package com.example.messengerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.messengerapp.data.local.entities.ChatEntity
import com.example.messengerapp.data.local.entities.ChatWithLastMessage
import com.example.messengerapp.data.local.entities.ChatWithPartnerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Upsert
    suspend fun upsertChats(chats:List<ChatEntity>)
    @Query("Select * from chats order by lastModified DESC")
    fun getChats(): Flow<List<ChatEntity>>
    @Query("Delete from chats")
    suspend fun clearAll()
    @Query("SELECT * FROM CHATS ORDER BY lastModified Desc")
    suspend fun getChatsOneShot(): List<ChatEntity>
    @Transaction
    @Query("""
    SELECT * FROM chats 
    ORDER BY lastModified DESC
""")
    fun getChatsWithPartners(): Flow<List<ChatWithPartnerEntity>>
    @Transaction
    @Query("""
        SELECT * FROM CHATS
        ORDER BY lastModified Desc
    """)
    fun getChatsWithLastMessages(): Flow<List<ChatWithLastMessage>>
}