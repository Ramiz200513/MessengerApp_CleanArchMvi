package com.example.domain.domain.repository

import com.example.domain.domain.model.User

interface ProfileRepository {
    suspend fun saveProfileEdit(user: User): Result<User>
}