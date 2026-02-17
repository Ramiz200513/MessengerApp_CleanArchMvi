package com.example.domain.domain.repository

import com.example.domain.domain.model.User

interface UserRepository {
    suspend fun searchUserByEmail(email: String):Result<User?>
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun updateFcmToken(token: String):Result<Unit>
}