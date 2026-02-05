package com.example.domain.domain.repository

import com.example.domain.domain.model.User

interface UserRepository {
    suspend fun searchUserByEmail(email: String):Result<User?>
}