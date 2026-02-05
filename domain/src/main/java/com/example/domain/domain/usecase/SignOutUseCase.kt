package com.example.domain.domain.usecase

import com.example.domain.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
){
    suspend operator fun invoke(){
        repository.signOut()
    }
}