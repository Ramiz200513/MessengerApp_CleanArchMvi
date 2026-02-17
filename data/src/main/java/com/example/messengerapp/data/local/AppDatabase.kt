package com.example.messengerapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.messengerapp.data.local.dao.ChatDao
import com.example.messengerapp.data.local.dao.MessageDao
import com.example.messengerapp.data.local.dao.UserDao
import com.example.messengerapp.data.local.entities.ChatEntity
import com.example.messengerapp.data.local.entities.MessageEntity
import com.example.messengerapp.data.local.entities.UserEntity

@Database(entities = [ChatEntity::class, MessageEntity::class, UserEntity::class],version = 3,exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    suspend fun clearAllTablesData() {
        chatDao().clearAll()
        messageDao().clearAll()
        userDao().clearAll()
    }
}