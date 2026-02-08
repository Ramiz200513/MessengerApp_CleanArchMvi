package com.example.domain.domain.repository

import com.example.domain.domain.model.User

interface AuthRepository {
    suspend fun signIn(email:String,password: String):Result<Unit>
    suspend fun signUp(email: String,password: String,username: String):Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun signOut()
}