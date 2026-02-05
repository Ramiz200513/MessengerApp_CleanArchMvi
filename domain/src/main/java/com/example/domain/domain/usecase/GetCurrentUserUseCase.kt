package com.example.domain.domain.usecase

import com.example.domain.domain.model.User
import com.example.domain.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> {
        return repository.getCurrentUser()
    }
}