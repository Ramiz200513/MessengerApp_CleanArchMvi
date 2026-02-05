package com.example.domain.domain.usecase

import com.example.domain.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email:String, password: String,username:String): Result<Unit> {
        return repository.signUp(email,password,username)
    }
}