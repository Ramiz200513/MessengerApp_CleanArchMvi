package com.example.domain.domain.repository

import android.net.Uri
import com.example.domain.domain.model.User

interface ProfileRepository {
    suspend fun uploadAvatar(imageUri: Uri):Result<String>
    suspend fun saveProfileEdit(user: User): Result<User>
}