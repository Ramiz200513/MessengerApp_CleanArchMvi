package com.example.domain.domain.usecase

import com.example.domain.domain.model.User
import com.example.domain.domain.repository.UserRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(userId:String):Result<User?>{
        return repository.getUserById(userId)
    }
}