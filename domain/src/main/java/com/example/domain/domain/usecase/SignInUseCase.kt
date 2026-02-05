package com.example.domain.domain.usecase

import com.example.domain.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email:String, password:String):Result<Unit>{
        return  repository.signIn(email,password)
    }
}