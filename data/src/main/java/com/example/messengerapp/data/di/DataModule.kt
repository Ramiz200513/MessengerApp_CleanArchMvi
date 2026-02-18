package com.example.messengerapp.data.di

import android.app.Application

import androidx.room.Room
import kotlinx.serialization.json.Json
import com.example.domain.domain.repository.AuthRepository
import com.example.domain.domain.repository.ChatRepository
import com.example.domain.domain.repository.ProfileRepository
import com.example.domain.domain.repository.UserRepository
import com.example.messengerapp.data.local.AppDatabase
import com.example.messengerapp.data.local.dao.ChatDao
import com.example.messengerapp.data.local.dao.MessageDao
import com.example.messengerapp.data.local.dao.UserDao
import com.example.messengerapp.data.network.FcmApi
import com.example.messengerapp.data.repository.FirebaseAuthRepositoryImpl
import com.example.messengerapp.data.repository.FirebaseChatRepositoryImpl
import com.example.messengerapp.data.repository.ProfileRepositoryImpl
import com.example.messengerapp.data.repository.UserRepositoryImpl
import okhttp3.MediaType.Companion.toMediaType
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFireStorage(): FirebaseStorage{
        return FirebaseStorage.getInstance()
    }
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
@Module
@InstallIn(SingletonComponent::class)
object RoomModule{
    @Provides
    @Singleton
    fun provideDatabase(App: Application): AppDatabase{
        return Room.databaseBuilder(App, AppDatabase::class.java, "messenger_db")
            .fallbackToDestructiveMigration()
            .build()

    }
    @Provides
    @Singleton
    fun provideChatDao(db: AppDatabase): ChatDao {
        return db.chatDao()
    }
    @Provides
    @Singleton
    fun provideMessageDao(db: AppDatabase): MessageDao{
        return db.messageDao()
    }
    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao{
        return db.userDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: FirebaseChatRepositoryImpl
    ): ChatRepository
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepositoryImpl
    ): AuthRepository
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFcmApi(): FcmApi {
        // 1. Создаем логгер
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }

        // 2. Создаем клиент с логгером
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        return Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .client(client) // <--- Добавляем клиент
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(FcmApi::class.java)
    }
}