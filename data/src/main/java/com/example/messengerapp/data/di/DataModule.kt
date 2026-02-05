package com.example.messengerapp.data.di

import com.example.domain.domain.repository.AuthRepository
import com.example.domain.domain.repository.ChatRepository
import com.example.domain.domain.repository.ProfileRepository
import com.example.domain.domain.repository.UserRepository
import com.example.messengerapp.data.repository.FirebaseAuthRepositoryImpl
import com.example.messengerapp.data.repository.FirebaseChatRepositoryImpl
import com.example.messengerapp.data.repository.ProfileRepositoryImpl
import com.example.messengerapp.data.repository.UserRepositoryImpl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

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